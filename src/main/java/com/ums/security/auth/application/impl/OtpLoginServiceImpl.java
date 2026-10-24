package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.util.PhoneNumbers;
import com.ums.security.auth.application.OtpLoginService;
import com.ums.security.auth.application.SmsSender;
import com.ums.security.user.domain.User;
import com.ums.security.user.infrastructure.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OtpLoginServiceImpl implements OtpLoginService {

    private static final String CODE_KEY = "auth:otp:";              // + userId → BCrypt hash of the code
    private static final String ATTEMPTS_KEY = "auth:otp:attempts:"; // + userId → wrong tries so far
    private static final Duration RESEND_GAP = Duration.ofSeconds(60);
    private static final String INVALID_CODE = "error.otp.invalid_code";

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SmsSender smsSender;
    private final MessageSource messageSource;
    private final Duration codeTtl;
    private final int maxAttempts;
    private final boolean logCode;

    public OtpLoginServiceImpl(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               RedisTemplate<String, Object> redisTemplate,
                               SmsSender smsSender,
                               MessageSource messageSource,
                               @Value("${ums.otp.code-ttl}") Duration codeTtl,
                               @Value("${ums.otp.max-attempts}") int maxAttempts,
                               @Value("${ums.otp.log-code:false}") boolean logCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.smsSender = smsSender;
        this.messageSource = messageSource;
        this.codeTtl = codeTtl;
        this.maxAttempts = maxAttempts;
        this.logCode = logCode;
    }

    /// SEND CODE ----------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public void sendCode(String phone) {
        Optional<User> found = findUser(phone);
        if (found.isEmpty()) {
            return;
        }
        User user = found.get();
        String codeKey = CODE_KEY + user.getId();

        // One SMS per minute: each one costs money, and nobody should flood a student's phone
        Long secondsLeft = redisTemplate.getExpire(codeKey);
        if (secondsLeft != null && secondsLeft > codeTtl.minus(RESEND_GAP).toSeconds()) {
            return;
        }

        String code = "%06d".formatted(random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(codeKey, passwordEncoder.encode(code), codeTtl);
        redisTemplate.delete(ATTEMPTS_KEY + user.getId());
        deliver(user, code);
    }

    /// VERIFY CODE --------------------------------------------------
    @Override
    public Long verifyCode(String phone, String code) {
        User user = findUser(phone).orElseThrow(() -> new BusinessException(INVALID_CODE));
        String codeKey = CODE_KEY + user.getId();
        String attemptsKey = ATTEMPTS_KEY + user.getId();

        Object codeHash = redisTemplate.opsForValue().get(codeKey);
        if (codeHash == null) {
            throw new BusinessException(INVALID_CODE);
        }
        // INCR is atomic, so guesses sent in parallel still can't get past the limit
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        redisTemplate.expire(attemptsKey, codeTtl);
        if (attempts == null || attempts > maxAttempts) {
            redisTemplate.delete(codeKey);
            redisTemplate.delete(attemptsKey);
            throw new BusinessException("error.otp.too_many_attempts");
        }
        if (!passwordEncoder.matches(code, codeHash.toString())) {
            throw new BusinessException(INVALID_CODE);
        }
        // delete() returns true only for the first caller, so one code gives exactly one login
        if (!Boolean.TRUE.equals(redisTemplate.delete(codeKey))) {
            throw new BusinessException(INVALID_CODE);
        }
        redisTemplate.delete(attemptsKey);
        return user.getId();
    }

    /// HELPERS ------------------------------------------------------

    /** Only ACTIVE accounts get codes: locked and disabled ones are treated like unknown numbers. */
    private Optional<User> findUser(String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            return Optional.empty();
        }
        return userRepository.findByPhone(normalized).filter(u -> u.getStatus() == User.Status.ACTIVE);
    }

    private void deliver(User user, String code) {
        if (logCode) {
            log.info("Login code for {}: {}", user.getUsername(), code);
        }
        // The user may not have opened the app in their language yet, so use the one saved on the account
        Locale locale = Locale.forLanguageTag(user.getPreferredLanguage());
        String text = messageSource.getMessage("sms.otp_login.body", new Object[] {code, codeTtl.toMinutes()}, locale);
        try {
            smsSender.send(user.getPhone(), text);
        } catch (RuntimeException ex) {
            log.error("Could not send the login code SMS to user {}", user.getId(), ex);
        }
    }
}
