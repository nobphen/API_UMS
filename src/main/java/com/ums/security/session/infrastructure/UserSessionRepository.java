package com.ums.security.session.infrastructure;

import com.ums.security.session.domain.UserSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /** The device's current session, if any: logging in again on the same device replaces it. */
    Optional<UserSession> findByUserIdAndDeviceIdAndRevokedAtIsNull(Long userId, String deviceId);

    /** The "Devices" list: sessions not logged out and not expired, most recently used first. */
    List<UserSession> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActiveAtDesc(Long userId, Instant now);

    List<UserSession> findByUserIdAndRevokedAtIsNull(Long userId);

    /** Cleanup job: sessions whose refresh token has run out. */
    @Modifying
    @Query(value = """
            UPDATE user_sessions SET revoked_at = now(), revoked_reason = 'EXPIRED'
            WHERE revoked_at IS NULL AND expires_at < now()
            """, nativeQuery = true)
    int revokeExpired();

    /** Cleanup job: sessions unused for longer than their user's session_ttl_days. */
    @Modifying
    @Query(value = """
            UPDATE user_sessions s SET revoked_at = now(), revoked_reason = 'INACTIVE'
            FROM users u
            WHERE u.id = s.user_id AND s.revoked_at IS NULL
              AND s.last_active_at < now() - make_interval(days => u.session_ttl_days)
            """, nativeQuery = true)
    int revokeInactive();
}
