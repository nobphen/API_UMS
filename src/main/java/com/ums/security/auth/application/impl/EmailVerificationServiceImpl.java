package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.mail.MailService;
import com.ums.security.auth.application.EmailVerificationService;
import com.ums.security.user.domain.User;
import com.ums.security.user.infrastructure.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The 6-digit email code that turns a PENDING sign-up into an ACTIVE account.
 * Only a BCrypt hash of the code is kept, in Redis, with a time limit and a limit on wrong tries.
 */
@Slf4j
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final String CODE_KEY = "auth:verify:";              // + userId → BCrypt hash of the code
    private static final String ATTEMPTS_KEY = "auth:verify:attempts:"; // + userId → wrong tries so far
    private static final Duration RESEND_GAP = Duration.ofSeconds(60);
    private static final String INVALID_CODE = "error.verify.invalid_code";

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MailService mailService;
    private final Duration codeTtl;
    private final int maxAttempts;
    private final boolean logCode;

    /** Spring passes the beans and the ums.email-verify.* settings from application.yml. */
    public EmailVerificationServiceImpl(UserRepository userRepository,
                                        PasswordEncoder passwordEncoder,
                                        RedisTemplate<String, Object> redisTemplate,
                                        MailService mailService,
                                        @Value("${ums.email-verify.code-ttl}") Duration codeTtl,
                                        @Value("${ums.email-verify.max-attempts}") int maxAttempts,
                                        @Value("${ums.email-verify.log-code:false}") boolean logCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.mailService = mailService;
        this.codeTtl = codeTtl;
        this.maxAttempts = maxAttempts;
        this.logCode = logCode;
    }

    /// SEND CODE ----------------------------------------------------
    /** Makes a 6-digit code, keeps only its hash in Redis (with a time limit) and emails it; at most one per minute. */
    @Override
    public void sendCode(User user) {
        String codeKey = CODE_KEY + user.getId();

        // One code per minute, so nobody can flood someone's inbox
        Long secondsLeft = redisTemplate.getExpire(codeKey);
        if (secondsLeft != null && secondsLeft > codeTtl.minus(RESEND_GAP).toSeconds()) {
            return;
        }

        String code = "%06d".formatted(random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(codeKey, passwordEncoder.encode(code), codeTtl);
        redisTemplate.delete(ATTEMPTS_KEY + user.getId());
        deliver(user, code);
    }

    /** Sends a new code only to a PENDING account; for any other email it does nothing (no hint who exists). */
    @Override
    @Transactional(readOnly = true)
    public void resendCode(String email) {
        findPendingUser(email).ifPresent(this::sendCode);
    }

    /// VERIFY -------------------------------------------------------
    /** Compares the code with the hash in Redis (limited tries); right code = account ACTIVE and the code is deleted. */
    @Override
    @Transactional
    public void verify(String email, String code) {
        User user = findPendingUser(email).orElseThrow(() -> new BusinessException(INVALID_CODE));
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
            throw new BusinessException("error.verify.too_many_attempts");
        }
        if (!passwordEncoder.matches(code, codeHash.toString())) {
            throw new BusinessException(INVALID_CODE);
        }
        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptsKey);

        user.setStatus(User.Status.ACTIVE);
    }

    /// HELPERS ------------------------------------------------------

    /** Only PENDING accounts need a code: active, locked and disabled ones are treated like unknown emails. */
    private Optional<User> findPendingUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(u -> u.getStatus() == User.Status.PENDING);
    }

    /** Emails the code (HTML, in the user's saved language) through MailService; no mail server = only a warning. */
    private void deliver(User user, String code) {
        if (logCode) {
            log.info("Email verification code for {}: {}", user.getUsername(), code);
        }
        // The user is not in the app when this arrives, so it uses their saved language, not the request's
        mailService.sendCode(user.getEmail(), user.getPreferredLanguage(), "email_verify", code, codeTtl.toMinutes());
    }
}
