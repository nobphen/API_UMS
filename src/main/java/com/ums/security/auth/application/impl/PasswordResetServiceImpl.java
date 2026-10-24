package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.security.auth.api.dto.ForgotPasswordRequest;
import com.ums.security.auth.api.dto.ResetPasswordRequest;
import com.ums.security.auth.application.PasswordResetService;
import com.ums.security.session.application.UserSessionService;
import com.ums.security.session.domain.UserSession;
import com.ums.security.user.domain.User;
import com.ums.security.user.infrastructure.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
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
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final String CODE_KEY = "auth:pwreset:";              // + userId → BCrypt hash of the code
    private static final String ATTEMPTS_KEY = "auth:pwreset:attempts:"; // + userId → wrong tries so far
    private static final Duration RESEND_GAP = Duration.ofSeconds(60);
    private static final String INVALID_CODE = "error.reset.invalid_code";

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionService userSessionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectProvider<JavaMailSender> mailSender;   // only exists when spring.mail.host is set
    private final MessageSource messageSource;
    private final Duration codeTtl;
    private final int maxAttempts;
    private final String mailFrom;
    private final boolean logCode;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    UserSessionService userSessionService,
                                    RedisTemplate<String, Object> redisTemplate,
                                    ObjectProvider<JavaMailSender> mailSender,
                                    MessageSource messageSource,
                                    @Value("${ums.password-reset.code-ttl}") Duration codeTtl,
                                    @Value("${ums.password-reset.max-attempts}") int maxAttempts,
                                    @Value("${ums.password-reset.mail-from}") String mailFrom,
                                    @Value("${ums.password-reset.log-code:false}") boolean logCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSessionService = userSessionService;
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
    @Transactional(readOnly = true)
    public void sendResetCode(ForgotPasswordRequest request) {
        Optional<User> found = findUser(request.username());
        if (found.isEmpty()) {
            return;
        }
        User user = found.get();
        String codeKey = CODE_KEY + user.getId();

        // One code per minute, so nobody can flood a student's inbox
        Long secondsLeft = redisTemplate.getExpire(codeKey);
        if (secondsLeft != null && secondsLeft > codeTtl.minus(RESEND_GAP).toSeconds()) {
            return;
        }

        String code = "%06d".formatted(random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(codeKey, passwordEncoder.encode(code), codeTtl);
        redisTemplate.delete(ATTEMPTS_KEY + user.getId());
        deliver(user, code);
    }

    /// RESET --------------------------------------------------------
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("error.password.mismatch");
        }
        User user = findUser(request.username()).orElseThrow(() -> new BusinessException(INVALID_CODE));
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
            throw new BusinessException("error.reset.too_many_attempts");
        }
        if (!passwordEncoder.matches(request.code(), codeHash.toString())) {
            throw new BusinessException(INVALID_CODE);
        }
        redisTemplate.delete(codeKey);
        redisTemplate.delete(attemptsKey);

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(OffsetDateTime.now());
        user.setFailedLoginCount(0);
        if (user.getStatus() == User.Status.LOCKED) {   // owning the email is enough to undo a wrong-password lock
            user.setStatus(User.Status.ACTIVE);
        }
        userSessionService.revokeAll(user.getId(), UserSession.RevokedReason.PASSWORD_CHANGED, user.getId(), null);
    }

    /// HELPERS ------------------------------------------------------

    /** DISABLED accounts (graduated, dropped) can't reset; they are treated like unknown accounts. */
    private Optional<User> findUser(String login) {
        return userRepository.findByLogin(login)
                .filter(u -> u.getStatus() != User.Status.DISABLED);
    }

    private void deliver(User user, String code) {
        if (logCode) {
            log.info("Password reset code for {}: {}", user.getUsername(), code);
        }
        if (user.getEmail() == null) {
            log.warn("Password reset asked for user {}, but the account has no email", user.getId());
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Password reset code for user {} not sent: no mail server configured (spring.mail.host)", user.getId());
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        // The user is not in the app when this arrives, so it uses their saved language, not the request's
        Locale locale = Locale.forLanguageTag(user.getPreferredLanguage());
        message.setSubject(messageSource.getMessage("mail.password_reset.subject", null, locale));
        message.setText(messageSource.getMessage("mail.password_reset.body", new Object[] {code, codeTtl.toMinutes()}, locale));
        try {
            sender.send(message);
        } catch (MailException ex) {
            log.error("Could not send password reset email to user {}", user.getId(), ex);
        }
    }
}
