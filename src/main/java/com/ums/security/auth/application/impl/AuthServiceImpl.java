package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.security.CurrentUser;
import com.ums.common.util.PhoneNumbers;
import com.ums.security.auth.AuthUser;
import com.ums.security.auth.api.dto.ChangePasswordRequest;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.auth.api.dto.LoginRequest;
import com.ums.security.auth.api.dto.OtpLoginRequest;
import com.ums.security.auth.api.dto.RefreshTokenRequest;
import com.ums.security.auth.api.dto.RegisterRequest;
import com.ums.security.auth.api.dto.RegisterResponse;
import com.ums.security.auth.api.dto.TokenResponse;
import com.ums.security.auth.application.AuthService;
import com.ums.security.auth.application.EmailVerificationService;
import com.ums.security.auth.application.JwtService;
import com.ums.security.auth.application.OtpLoginService;
import com.ums.security.auth.application.PermissionCacheService;
import com.ums.security.auth.application.QrLoginService;
import com.ums.security.session.application.UserSessionService;
import com.ums.security.session.domain.UserSession;
import com.ums.security.role.domain.Role;
import com.ums.security.role.infrastructure.RoleRepository;
import com.ums.security.user.domain.User;
import com.ums.security.user.domain.UserRole;
import com.ums.security.user.infrastructure.UserRepository;
import com.ums.security.user.infrastructure.UserRoleRepository;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sign-up, every kind of login (password, SMS code, QR), token refresh, logout and change password.
 * Each login opens a user_sessions row and gives an access + refresh token for it.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_LOGINS = 5;
    private static final String APPLICANT_ROLE = "APPLICANT";

    private final DaoAuthenticationProvider daoAuthenticationProvider;   // from SecurityConfig
    private final JwtAuthenticationProvider jwtAuthenticationProvider;   // from SecurityConfig (checks refresh tokens)
    private final PasswordEncoder passwordEncoder;                       // from PasswordEncoderConfig
    private final JwtService jwtService;
    private final UserSessionService userSessionService;
    private final PermissionCacheService permissionCacheService;
    private final QrLoginService qrLoginService;
    private final OtpLoginService otpLoginService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    /// REGISTER -----------------------------------------------------
    /**
     * New applicant account: checks password, username, email and phone are free, saves the user with the APPLICANT role.
     */
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. CHECKS ONLY ---------------------------------------------
        // 1a. Password and confirm password must be the same
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("error.password.mismatch");
        }

        // 1b. Username and email must be free (email saved in lower case)
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("error.user.username_taken", username);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("error.user.email_taken");
        }

        // 1c. Phone must be free too: it is also a login name
        String phone = PhoneNumbers.normalizeForSave(request.phone());
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BusinessException("error.user.phone_taken");
        }

        // 1d. The APPLICANT role comes from the seed data; missing = a setup mistake
        Role applicantRole = roleRepository.findByCode(APPLICANT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role APPLICANT is missing; check V3__seed_roles_and_admin.sql"));

        // 2. Save the user (password hashed with BCrypt, type APPLICANT)
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setUserType(User.UserType.APPLICANT);
        // TODO email verify: turned off for now (user is ACTIVE by default); uncomment before going live
        // user.setStatus(User.Status.PENDING);   // can't log in until the email is verified
        userRepository.save(user);

        // 3. Give the user the APPLICANT role
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(applicantRole);
        userRoleRepository.save(userRole);

        // 4. No tokens here: the applicant types the emailed code (/verify-email), then logs in
        // TODO email verify: turned off for now; uncomment before going live
        // emailVerificationService.sendCode(user);
        return new RegisterResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    /// LOGIN --------------------------------------------------------
    /**
     * Spring checks the password; a wrong one adds 1 to the failed count, a right one resets it and opens a session.
     */
    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)   // keep the failed-login count on a wrong password
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // 1. Spring checks the login. Inside authenticate():
        //    loadUserByUsername() → isAccountNonLocked() → isEnabled() → password (BCrypt).
        //    Any check fails → it throws; all pass → it returns auth (= login success)
        Authentication auth;
        try {
            auth = daoAuthenticationProvider.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            // 1a. Wrong password or unknown user: count it (5 times → LOCKED), then fail
            recordFailedLogin(request.username());
            throw ex;
        } catch (DisabledException ex) {
            // 1b. Spring says "disabled" for PENDING too; tell the applicant to verify the email instead
            boolean pending = userRepository.findByLogin(request.username())
                    .filter(u -> u.getStatus() == User.Status.PENDING)
                    .isPresent();
            throw pending ? new DisabledException("auth.account_pending", ex) : ex;
        }

        // 2. Login OK: the principal is the AuthUser made in CustomUserDetailsService.loadUserByUsername()
        AuthUser authUser = (AuthUser) auth.getPrincipal();
        assert authUser != null;

        // 3. Reset the failed count and save the login time (AuthUser is read-only, so load the real User)
        User user = userRepository.findById(authUser.getId()).orElseThrow();
        user.setFailedLoginCount(0);
        user.setLastLoginAt(OffsetDateTime.now());

        // 4. Roles + permissions as plain text (ROLE_SUPER_ADMIN, STAFF_VIEW ...) for the access token
        Set<String> authorities = authUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 5. rememberMe empty = true
        boolean rememberMe = request.rememberMe() == null || request.rememberMe();

        // 6. Save a user_sessions row and give the access + refresh token
        return openSession(user, authorities, request.device(), ipAddress, userAgent,
                UserSession.LoginMethod.PASSWORD, rememberMe);
    }

    /// OTP LOGIN ----------------------------------------------------
    /**
     * Checks the SMS code, then the account must be ACTIVE; opens a session like a password login.
     */
    @Override
    @Transactional
    public TokenResponse loginWithOtp(OtpLoginRequest request, String ipAddress, String userAgent) {
        // 1. Check the SMS code; gives back the user id of that phone
        Long userId = otpLoginService.verifyCode(request.phone(), request.code());

        // 2. The user must exist and be ACTIVE (no Spring here, so check the status by hand)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("auth.user_not_found"));
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new DisabledException("auth.account_" + user.getStatus().name().toLowerCase());
        }

        // 3. Reset the failed count and save the login time
        user.setFailedLoginCount(0);
        user.setLastLoginAt(OffsetDateTime.now());

        // 4. rememberMe empty = true; permissions fresh from the database
        boolean rememberMe = request.rememberMe() == null || request.rememberMe();

        // 5. Save a user_sessions row and give the tokens
        return openSession(user, permissionCacheService.reload(userId), request.device(), ipAddress, userAgent,
                UserSession.LoginMethod.OTP, rememberMe);
    }

    /// REFRESH ------------------------------------------------------
    /**
     * Checks the refresh token and its session, then gives a new access and refresh token (rotation).
     */
    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)   // keep the TOKEN_REUSE revoke
    public TokenResponse refresh(RefreshTokenRequest request) {
        // 1. Spring checks the token signature and expiry
        JwtAuthenticationToken auth = (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(
                new BearerTokenAuthenticationToken(request.refreshToken()));
        Jwt jwt = auth.getToken();

        // 2. It must be a refresh token, not an access token
        if (!"refresh".equals(jwt.getClaimAsString("typ"))) {
            throw new BadCredentialsException("auth.not_refresh_token");
        }

        // 3. The session (sid) must still be open and hold this same refresh token
        Long sessionId = Long.valueOf(Objects.requireNonNull(jwt.getClaimAsString("sid")));
        UserSession session = userSessionService.getActiveSession(sessionId, request.refreshToken());

        // 4. The user must still be ACTIVE (Spring's isAccountNonLocked / isEnabled are not called here);
        //    LOCKED / DISABLED → end this session too
        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new BadCredentialsException("auth.user_not_found"));
        if (user.getStatus() != User.Status.ACTIVE) {
            userSessionService.revoke(sessionId, UserSession.RevokedReason.USER_LOCKED, null);
            throw new DisabledException("auth.account_" + user.getStatus().name().toLowerCase());
        }

        // 5. New access + refresh token on the same session (permissions from the Redis cache)
        return issueTokens(user, session, permissionCacheService.getAuthorities(user.getId()));
    }

    /// LOGOUT -------------------------------------------------------
    /**
     * Revokes only the session of this token (this device); other devices stay logged in.
     */
    @Override
    @Transactional
    public void logout(Jwt accessToken) {
        // 1. Session id (sid) and user id (sub) from the access token
        Long sessionId = Long.valueOf(Objects.requireNonNull(accessToken.getClaimAsString("sid")));
        Long userId = CurrentUser.userId(accessToken);

        // 2. End only this session; the token stops working at the next request (SessionActivityFilter)
        userSessionService.revoke(sessionId, UserSession.RevokedReason.LOGOUT, userId);
    }

    /// QR LOGIN -----------------------------------------------------
    /**
     * Empty while the QR is not approved yet; once approved (single use), opens a session for that user.
     */
    @Override
    @Transactional
    public Optional<TokenResponse> loginWithQr(String qrId, DeviceInfo device, String ipAddress, String userAgent) {
        // 1. Not approved by the phone yet → empty (the web asks again later)
        return qrLoginService.consumeApproved(qrId).map(userId -> {
            // 2. The user must exist and be ACTIVE
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("auth.user_not_found"));
            if (user.getStatus() != User.Status.ACTIVE) {
                throw new DisabledException("auth.account_" + user.getStatus().name().toLowerCase());
            }

            // 3. Save the login time
            user.setLastLoginAt(OffsetDateTime.now());

            // 4. Save a user_sessions row and give the tokens (rememberMe always on)
            return openSession(user, permissionCacheService.reload(userId), device, ipAddress, userAgent,
                    UserSession.LoginMethod.QR, true);
        });
    }

    /// CHANGE PASSWORD ----------------------------------------------
    /**
     * Checks the current password, saves the new one, logs out every other device and gives new tokens.
     */
    @Override
    @Transactional
    public TokenResponse changePassword(Jwt accessToken, ChangePasswordRequest request) {
        // 1. User id and session id from the access token, then load the user
        Long userId = CurrentUser.userId(accessToken);
        Long sessionId = Long.valueOf(Objects.requireNonNull(accessToken.getClaimAsString("sid")));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("auth.user_not_found"));

        // 2. CHECKS ONLY ---------------------------------------------
        // 2a. Current password must be right. A BusinessException (400), not 401: the app must not treat a typo as "logged out"
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("error.password.current_wrong");
        }

        // 2b. New password and confirm password must be the same
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("error.password.mismatch");
        }

        // 2c. New password must be different from the current one
        if (request.newPassword().equals(request.currentPassword())) {
            throw new BusinessException("error.password.same_as_current");
        }

        // 3. Save the new password (hashed); the temporary-password flag goes off
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(OffsetDateTime.now());

        // 4. Log out every other device, keep this one
        UserSession session = userSessionService.getActiveSession(sessionId);
        userSessionService.revokeAll(userId, UserSession.RevokedReason.PASSWORD_CHANGED, userId, sessionId);

        // 5. New tokens for this device (the old token still has pwd_change=true)
        return issueTokens(user, session, permissionCacheService.getAuthorities(userId));
    }

    /// HELPERS ------------------------------------------------------
    /**
     * Saves a new session (device, IP, login method), then makes its tokens.
     */
    private TokenResponse openSession(User user, Set<String> authorities, DeviceInfo device, String ipAddress,
                                      String userAgent, UserSession.LoginMethod method, boolean rememberMe) {
        // 1. Save a user_sessions row (device, IP, how they logged in, when it expires)
        UserSession session = userSessionService.create(user.getId(), device, ipAddress, userAgent,
                method, rememberMe, jwtService.getRefreshTokenTtl());

        // 2. Make the tokens for this session
        return issueTokens(user, session, authorities);
    }

    /**
     * Makes the access and refresh token for the session and stores the refresh token (hashed) on it.
     */
    private TokenResponse issueTokens(User user, UserSession session, Set<String> authorities) {
        // 1. Access token (short, holds user id, sid and permissions) + refresh token (long, holds user id and sid)
        String accessToken = jwtService.generateAccessToken(user, session.getId(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), session.getId());

        // 2. Keep the refresh token (hashed) on the session, so an old one can't be used again
        userSessionService.updateRefreshToken(session, refreshToken, jwtService.getRefreshTokenTtl());

        // 3. The answer the app gets: both tokens, their lifetimes in seconds, and "must change password"
        return new TokenResponse(accessToken, refreshToken, "Bearer",
                jwtService.getAccessTokenTtl().toSeconds(), jwtService.getRefreshTokenTtl().toSeconds(),
                user.isMustChangePassword());
    }

    /**
     * Adds 1 to the failed-login count; at MAX_FAILED_LOGINS the account is LOCKED. Unknown login: nothing.
     */
    private void recordFailedLogin(String login) {
        userRepository.findByLogin(login).ifPresent(user -> {
            // 1. One more wrong password
            user.setFailedLoginCount(user.getFailedLoginCount() + 1);

            // 2. 5 wrong in a row → LOCKED (unlock: reset password, or an admin unlocks)
            if (user.getFailedLoginCount() >= MAX_FAILED_LOGINS) {
                user.setStatus(User.Status.LOCKED);
            }
        });
    }
}
