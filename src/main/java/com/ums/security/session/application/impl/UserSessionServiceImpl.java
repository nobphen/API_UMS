package com.ums.security.session.application.impl;

import com.ums.common.exception.NotFoundException;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.session.application.UserSessionService;
import com.ums.security.session.domain.UserSession;
import com.ums.security.session.infrastructure.UserSessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserSessionServiceImpl implements UserSessionService {

    private static final String REVOKED_KEY = "session:revoked:";
    private static final int USER_AGENT_MAX = 500;

    private final UserSessionRepository userSessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration accessTokenTtl;

    public UserSessionServiceImpl(UserSessionRepository userSessionRepository,
                                  RedisTemplate<String, Object> redisTemplate,
                                  @Value("${ums.jwt.access-token-ttl}") Duration accessTokenTtl) {
        this.userSessionRepository = userSessionRepository;
        this.redisTemplate = redisTemplate;
        this.accessTokenTtl = accessTokenTtl;
    }

    @Override
    public UserSession create(Long userId, DeviceInfo device, String ipAddress, String userAgent,
                              UserSession.LoginMethod loginMethod, boolean rememberMe, Duration ttl) {
        if (device.deviceId() != null) {
            userSessionRepository.findByUserIdAndDeviceIdAndRevokedAtIsNull(userId, device.deviceId())
                    .ifPresent(old -> {
                        markRevoked(old, UserSession.RevokedReason.REPLACED, userId);
                        userSessionRepository.saveAndFlush(old);   // free the (user, device) unique index before the insert
                    });
        }

        Instant now = Instant.now();
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setRefreshTokenHash("pending:" + UUID.randomUUID());   // replaced by updateRefreshToken once the id exists
        session.setDeviceId(device.deviceId());
        session.setDeviceModel(device.deviceModel());
        session.setPlatform(device.platform());
        session.setOsVersion(device.osVersion());
        session.setAppName(device.appName());
        session.setAppVersion(device.appVersion());
        session.setFcmToken(device.fcmToken());
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), USER_AGENT_MAX)));
        session.setLoginMethod(loginMethod);
        session.setRememberMe(rememberMe);
        session.setLastActiveAt(now);
        session.setExpiresAt(now.plus(ttl));
        return userSessionRepository.save(session);
    }

    @Override
    public void updateRefreshToken(UserSession session, String refreshToken, Duration ttl) {
        Instant now = Instant.now();
        session.setRefreshTokenHash(hash(refreshToken));
        session.setLastActiveAt(now);
        session.setExpiresAt(now.plus(ttl));
    }

    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)
    public UserSession getActiveSession(Long sessionId, String refreshToken) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BadCredentialsException("Session not found"));
        if (!session.isActive()) {
            throw new BadCredentialsException("Session has expired or was logged out");
        }
        if (!session.getRefreshTokenHash().equals(hash(refreshToken))) {
            markRevoked(session, UserSession.RevokedReason.TOKEN_REUSE, null);
            throw new BadCredentialsException("Refresh token was already used; please log in again");
        }
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSession getActiveSession(Long sessionId) {
        return userSessionRepository.findById(sessionId)
                .filter(UserSession::isActive)
                .orElseThrow(() -> new BadCredentialsException("Session has expired or was logged out"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessions(Long userId) {
        return userSessionRepository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActiveAtDesc(
                userId, Instant.now());
    }

    @Override
    public void revoke(Long sessionId, UserSession.RevokedReason reason, Long revokedBy) {
        userSessionRepository.findById(sessionId)
                .filter(s -> s.getRevokedAt() == null)
                .ifPresent(s -> markRevoked(s, reason, revokedBy));
    }

    @Override
    public void revokeOwn(Long userId, Long sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .filter(s -> s.getUserId().equals(userId) && s.isActive())
                .orElseThrow(() -> new NotFoundException("Session not found"));
        markRevoked(session, UserSession.RevokedReason.TERMINATED_BY_USER, userId);
    }

    @Override
    public int revokeAll(Long userId, UserSession.RevokedReason reason, Long revokedBy, Long keepSessionId) {
        List<UserSession> sessions = userSessionRepository.findByUserIdAndRevokedAtIsNull(userId).stream()
                .filter(s -> !Objects.equals(s.getId(), keepSessionId))
                .toList();
        sessions.forEach(s -> markRevoked(s, reason, revokedBy));
        return sessions.size();
    }

    @Override
    public int revokeExpiredAndInactive() {
        // No Redis entry needed: these sessions have not refreshed for a long time, so their access tokens are already dead
        return userSessionRepository.revokeExpired() + userSessionRepository.revokeInactive();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRevoked(Long sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REVOKED_KEY + sessionId));
    }

    private void markRevoked(UserSession session, UserSession.RevokedReason reason, Long revokedBy) {
        session.setRevokedAt(Instant.now());
        session.setRevokedReason(reason);
        session.setRevokedBy(revokedBy);
        // Access tokens of this session stay valid until they expire, so remember the revoke that long
        redisTemplate.opsForValue().set(REVOKED_KEY + session.getId(), reason.name(), accessTokenTtl);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
