package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.security.auth.application.EmailVerificationService;
import com.ums.security.user.domain.User;
import com.ums.security.user.infrastructure.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ObjectProvider<JavaMailSender> mailSender;   // only exists when spring.mail.host is set
    private final MessageSource messageSource;
    private final Duration codeTtl;
    private final int maxAttempts;
    private final String mailFrom;
    private final boolean logCode;

    public EmailVerificationServiceImpl(UserRepository userRepository,
                                        PasswordEncoder passwordEncoder,
                                        RedisTemplate<String, Object> redisTemplate,
                                        ObjectProvider<JavaMailSender> mailSender,
                                        MessageSource messageSource,
                                        @Value("${ums.email-verify.code-ttl}") Duration codeTtl,
                                        @Value("${ums.email-verify.max-attempts}") int maxAttempts,
                                        @Value("${ums.email-verify.mail-from}") String mailFrom,
                                        @Value("${ums.email-verify.log-code:false}") boolean logCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.messageSource = messageSource;
        this.codeTtl = codeTtl;
        this.maxAttempts = maxAttempts;
        this.mailFrom = mailFrom;
        this.logCode = logCode;
    }

    /// SEND CODE ----------------------------------------------------
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

    @Override
    @Transactional(readOnly = true)
    public void resendCode(String email) {
        findPendingUser(email).ifPresent(this::sendCode);
    }

    /// VERIFY -------------------------------------------------------
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

    private void deliver(User user, String code) {
        if (logCode) {
            log.info("Email verification code for {}: {}", user.getUsername(), code);
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Verification code for user {} not sent: no mail server configured (spring.mail.host)", user.getId());
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        // The user is not in the app when this arrives, so it uses their saved language, not the request's
        Locale locale = Locale.forLanguageTag(user.getPreferredLanguage());
        message.setSubject(messageSource.getMessage("mail.email_verify.subject", null, locale));
        message.setText(messageSource.getMessage("mail.email_verify.body", new Object[] {code, codeTtl.toMinutes()}, locale));
        try {
            sender.send(message);
        } catch (MailException ex) {
            log.error("Could not send verification email to user {}", user.getId(), ex);
        }
    }
}
