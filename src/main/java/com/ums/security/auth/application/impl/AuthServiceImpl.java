package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.security.auth.AuthUser;
import com.ums.security.auth.api.dto.ChangePasswordRequest;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.auth.api.dto.LoginRequest;
import com.ums.security.auth.api.dto.RefreshTokenRequest;
import com.ums.security.auth.api.dto.RegisterRequest;
import com.ums.security.auth.api.dto.TokenResponse;
import com.ums.security.auth.application.AuthService;
import com.ums.security.auth.application.JwtService;
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
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    /// REGISTER -----------------------------------------------------
    @Override
    @Transactional
    public TokenResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email is already registered");
        }
        Role applicantRole = roleRepository.findByCode(APPLICANT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role APPLICANT is missing; check V3__seed_roles_and_admin.sql"));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setUserType(User.UserType.APPLICANT);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(applicantRole);
        userRoleRepository.save(userRole);

        // Logged in straight away: same session + tokens as a normal login
        return openSession(user, permissionCacheService.reload(user.getId()), request.device(), ipAddress, userAgent,
                UserSession.LoginMethod.PASSWORD, true);
    }

    /// LOGIN --------------------------------------------------------
    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)   // keep the failed-login count on a wrong password
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Authentication auth;
        try {
            auth = daoAuthenticationProvider.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            recordFailedLogin(request.username());
            throw ex;
        }

        AuthUser authUser = (AuthUser) auth.getPrincipal();
        assert authUser != null;
        User user = userRepository.findById(authUser.getId()).orElseThrow();
        user.setFailedLoginCount(0);
        user.setLastLoginAt(OffsetDateTime.now());

        Set<String> authorities = authUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean rememberMe = request.rememberMe() == null || request.rememberMe();
        return openSession(user, authorities, request.device(), ipAddress, userAgent,
                UserSession.LoginMethod.PASSWORD, rememberMe);
    }

    /// REFRESH ------------------------------------------------------
    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)   // keep the TOKEN_REUSE revoke
    public TokenResponse refresh(RefreshTokenRequest request) {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(
                new BearerTokenAuthenticationToken(request.refreshToken()));
        Jwt jwt = auth.getToken();
        if (!"refresh".equals(jwt.getClaimAsString("typ"))) {
            throw new BadCredentialsException("Not a refresh token");
        }

        Long sessionId = Long.valueOf(Objects.requireNonNull(jwt.getClaimAsString("sid")));
        UserSession session = userSessionService.getActiveSession(sessionId, request.refreshToken());

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        if (user.getStatus() != User.Status.ACTIVE) {
            userSessionService.revoke(sessionId, UserSession.RevokedReason.USER_LOCKED, null);
            throw new DisabledException("Account is " + user.getStatus().name().toLowerCase());
        }

        return issueTokens(user, session, permissionCacheService.getAuthorities(user.getId()));
    }

    /// LOGOUT -------------------------------------------------------
    @Override
    @Transactional
    public void logout(Jwt accessToken) {
        Long sessionId = Long.valueOf(Objects.requireNonNull(accessToken.getClaimAsString("sid")));
        Long userId = Long.valueOf(Objects.requireNonNull(accessToken.getSubject()));
        userSessionService.revoke(sessionId, UserSession.RevokedReason.LOGOUT, userId);
    }

    /// QR LOGIN -----------------------------------------------------
    @Override
    @Transactional
    public Optional<TokenResponse> loginWithQr(String qrId, DeviceInfo device, String ipAddress, String userAgent) {
        return qrLoginService.consumeApproved(qrId).map(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));
            if (user.getStatus() != User.Status.ACTIVE) {
                throw new DisabledException("Account is " + user.getStatus().name().toLowerCase());
            }
            user.setLastLoginAt(OffsetDateTime.now());
            return openSession(user, permissionCacheService.reload(userId), device, ipAddress, userAgent,
                    UserSession.LoginMethod.QR, true);
        });
    }

    /// CHANGE PASSWORD ----------------------------------------------
    @Override
    @Transactional
    public TokenResponse changePassword(Jwt accessToken, ChangePasswordRequest request) {
        Long userId = Long.valueOf(Objects.requireNonNull(accessToken.getSubject()));
        Long sessionId = Long.valueOf(Objects.requireNonNull(accessToken.getClaimAsString("sid")));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // A BusinessException (400), not 401: the app must not treat a typo as "logged out"
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        if (request.newPassword().equals(request.currentPassword())) {
            throw new BusinessException("New password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setPasswordChangedAt(OffsetDateTime.now());

        UserSession session = userSessionService.getActiveSession(sessionId);
        userSessionService.revokeAll(userId, UserSession.RevokedReason.PASSWORD_CHANGED, userId, sessionId);
        return issueTokens(user, session, permissionCacheService.getAuthorities(userId));
    }

    /// HELPERS ------------------------------------------------------
    private TokenResponse openSession(User user, Set<String> authorities, DeviceInfo device, String ipAddress,
                                      String userAgent, UserSession.LoginMethod method, boolean rememberMe) {

        UserSession session = userSessionService.create(user.getId(), device, ipAddress, userAgent,
                method, rememberMe, jwtService.getRefreshTokenTtl());
        return issueTokens(user, session, authorities);
    }

    private TokenResponse issueTokens(User user, UserSession session, Set<String> authorities) {

        String accessToken = jwtService.generateAccessToken(user, session.getId(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), session.getId());

        userSessionService.updateRefreshToken(session, refreshToken, jwtService.getRefreshTokenTtl());

        return new TokenResponse(accessToken, refreshToken, "Bearer",
                jwtService.getAccessTokenTtl().toSeconds(), jwtService.getRefreshTokenTtl().toSeconds(),
                user.isMustChangePassword());
    }

    private void recordFailedLogin(String usernameOrEmail) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).ifPresent(user -> {
            user.setFailedLoginCount(user.getFailedLoginCount() + 1);
            if (user.getFailedLoginCount() >= MAX_FAILED_LOGINS) {
                user.setStatus(User.Status.LOCKED);
            }
        });
    }
}
