package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.mail.MailService;
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
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Forgot password: emails a 6-digit code, then sets a new password with that code.
 * Only a BCrypt hash of the code is kept, in Redis, with a time limit and a limit on wrong tries.
 */
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
    private final MailService mailService;
    private final Duration codeTtl;
    private final int maxAttempts;
    private final boolean logCode;

    /** Spring passes the beans and the ums.password-reset.* settings from application.yml. */
    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    UserSessionService userSessionService,
                                    RedisTemplate<String, Object> redisTemplate,
                                    MailService mailService,
                                    @Value("${ums.password-reset.code-ttl}") Duration codeTtl,
                                    @Value("${ums.password-reset.max-attempts}") int maxAttempts,
                                    @Value("${ums.password-reset.log-code:false}") boolean logCode) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSessionService = userSessionService;
        this.redisTemplate = redisTemplate;
        this.mailService = mailService;
        this.codeTtl = codeTtl;
        this.maxAttempts = maxAttempts;
        this.logCode = logCode;
    }

    /// SEND CODE ----------------------------------------------------
    /** Makes a 6-digit code, keeps only its hash in Redis and emails it; at most one per minute. Unknown account: nothing. */
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
    /** Checks the code (limited tries), saves the new password, unlocks a LOCKED account and logs out every device. */
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

    /** Emails the code (HTML, in the user's saved language) through MailService; no email = only a warning in the log. */
    private void deliver(User user, String code) {
        if (logCode) {
            log.info("Password reset code for {}: {}", user.getUsername(), code);
        }
        if (user.getEmail() == null) {
            log.warn("Password reset asked for user {}, but the account has no email", user.getId());
            return;
        }
        // The user is not in the app when this arrives, so it uses their saved language, not the request's
        mailService.sendCode(user.getEmail(), user.getPreferredLanguage(), "password_reset", code, codeTtl.toMinutes());
    }
}
