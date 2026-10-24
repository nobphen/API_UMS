package com.ums.security.session.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.session.api.dto.LoginStatsResponse;
import com.ums.security.session.api.dto.UserLoginStatsResponse;
import com.ums.security.session.application.UserSessionService;
import com.ums.security.session.domain.UserSession;
import com.ums.security.session.infrastructure.UserSessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    private static final ZoneId CAMBODIA = ZoneId.of("Asia/Phnom_Penh");   // a "day" in the login stats
    private static final int DEFAULT_STATS_DAYS = 7;
    private static final int MAX_STATS_DAYS = 366;

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
                .orElseThrow(() -> new BadCredentialsException("auth.session_not_found"));
        if (!session.isActive()) {
            throw new BadCredentialsException("auth.session_ended");
        }
        if (!session.getRefreshTokenHash().equals(hash(refreshToken))) {
            markRevoked(session, UserSession.RevokedReason.TOKEN_REUSE, null);
            throw new BadCredentialsException("auth.refresh_reused");
        }
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSession getActiveSession(Long sessionId) {
        return userSessionRepository.findById(sessionId)
                .filter(UserSession::isActive)
                .orElseThrow(() -> new BadCredentialsException("auth.session_ended"));
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
        markRevoked(activeSessionOf(userId, sessionId), UserSession.RevokedReason.TERMINATED_BY_USER, userId);
    }

    @Override
    public void revokeByAdmin(Long userId, Long sessionId, Long adminId) {
        markRevoked(activeSessionOf(userId, sessionId), UserSession.RevokedReason.ADMIN, adminId);
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

    @Override
    @Transactional(readOnly = true)
    public List<LoginStatsResponse> loginStats(LocalDate from, LocalDate to) {
        LocalDate lastDay = to != null ? to : LocalDate.now(CAMBODIA);
        LocalDate firstDay = from != null ? from : lastDay.minusDays(DEFAULT_STATS_DAYS - 1);
        if (firstDay.isAfter(lastDay)) {
            throw new BusinessException("error.stats.from_after_to");
        }
        if (ChronoUnit.DAYS.between(firstDay, lastDay) >= MAX_STATS_DAYS) {
            throw new BusinessException("error.stats.too_many_days", MAX_STATS_DAYS);
        }
        return userSessionRepository.countLoginsPerDay(firstDay, lastDay, CAMBODIA.getId()).stream()
                .map(row -> new LoginStatsResponse(row.getDay(), row.getLogins(), row.getLogouts()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserLoginStatsResponse> loginStatsByUser(LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now(CAMBODIA);
        Instant start = day.atStartOfDay(CAMBODIA).toInstant();
        Instant end = day.plusDays(1).atStartOfDay(CAMBODIA).toInstant();
        return userSessionRepository.countLoginsPerUser(start, end).stream()
                .map(row -> new UserLoginStatsResponse(row.getUserId(), row.getUsername(), row.getUserType(),
                        row.getLogins(), row.getLogouts()))
                .toList();
    }

    /** The user's session if it is still active; someone else's session id is "not found", not "forbidden". */
    private UserSession activeSessionOf(Long userId, Long sessionId) {
        return userSessionRepository.findById(sessionId)
                .filter(s -> s.getUserId().equals(userId) && s.isActive())
                .orElseThrow(() -> new NotFoundException("error.session.not_found"));
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
