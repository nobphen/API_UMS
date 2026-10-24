package com.ums.security.session.infrastructure;

import com.ums.security.session.domain.UserSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * The device's current session, if any: logging in again on the same device replaces it.
     * <pre>
     * SELECT us.* FROM user_sessions us
     * WHERE us.user_id = ? AND us.device_id = ? AND us.revoked_at IS NULL
     * </pre>
     *
     * @param userId   id of the login (users.id)
     * @param deviceId id the app sends for the device
     * @return the open session on that device, or empty
     */
    Optional<UserSession> findByUserIdAndDeviceIdAndRevokedAtIsNull(Long userId, String deviceId);

    /**
     * The "Devices" list: sessions not logged out and not expired, most recently used first.
     * <pre>
     * SELECT us.* FROM user_sessions us
     * WHERE us.user_id = ? AND us.revoked_at IS NULL AND us.expires_at > ?
     * ORDER BY us.last_active_at DESC
     * </pre>
     *
     * @param userId id of the login (users.id)
     * @param now    current time; sessions expiring before it are left out
     * @return the open sessions, most recently used first
     */
    List<UserSession> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActiveAtDesc(Long userId, Instant now);

    /**
     * Every session of a login that has not been logged out, expired or not.
     * <pre>
     * SELECT us.* FROM user_sessions us WHERE us.user_id = ? AND us.revoked_at IS NULL
     * </pre>
     *
     * @param userId id of the login (users.id)
     * @return the sessions not yet revoked
     */
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

    /**
     * Logins and logouts for each day from..to, counted by the local day in the given time zone
     * (sessions store UTC, so a login at 06:00 in Phnom Penh still counts on that day). Days with none show 0.
     * A login is a new session (password, QR or SMS code); a logout is a session the user ended themselves.
     * <pre>
     * SELECT CAST(d.day AS date) AS day,
     *        (SELECT count(*) FROM user_sessions s
     *         WHERE s.created_at &gt;= d.day AT TIME ZONE ? AND s.created_at &lt; (d.day + interval '1 day') AT TIME ZONE ?) AS logins,
     *        (SELECT count(*) FROM user_sessions s
     *         WHERE s.revoked_reason = 'LOGOUT'
     *           AND s.revoked_at &gt;= d.day AT TIME ZONE ? AND s.revoked_at &lt; (d.day + interval '1 day') AT TIME ZONE ?) AS logouts
     * FROM generate_series(CAST(? AS date), CAST(? AS date), interval '1 day') AS d(day)
     * ORDER BY day
     * </pre>
     *
     * @param from     first day
     * @param to       last day (included)
     * @param timeZone time zone the days are in, e.g. Asia/Phnom_Penh
     * @return one row per day, oldest first
     */
    @Query(value = """
            SELECT CAST(d.day AS date) AS day,
                   (SELECT count(*) FROM user_sessions s
                    WHERE s.created_at >= d.day AT TIME ZONE :timeZone
                      AND s.created_at < (d.day + interval '1 day') AT TIME ZONE :timeZone) AS logins,
                   (SELECT count(*) FROM user_sessions s
                    WHERE s.revoked_reason = 'LOGOUT'
                      AND s.revoked_at >= d.day AT TIME ZONE :timeZone
                      AND s.revoked_at < (d.day + interval '1 day') AT TIME ZONE :timeZone) AS logouts
            FROM generate_series(CAST(:from AS date), CAST(:to AS date), interval '1 day') AS d(day)
            ORDER BY day
            """, nativeQuery = true)
    List<DailyLoginCount> countLoginsPerDay(LocalDate from, LocalDate to, String timeZone);

    /** One row of {@link #countLoginsPerDay}. */
    interface DailyLoginCount {
        LocalDate getDay();
        long getLogins();
        long getLogouts();
    }

    /**
     * Logins and logouts of each user between start and end, most logins first. Only users who logged in or
     * out in that time are listed; a logout is a session the user ended themselves.
     * <pre>
     * SELECT u.id AS user_id, u.username, u.user_type,
     *        count(*) FILTER (WHERE s.created_at &gt;= ? AND s.created_at &lt; ?) AS logins,
     *        count(*) FILTER (WHERE s.revoked_reason = 'LOGOUT' AND s.revoked_at &gt;= ? AND s.revoked_at &lt; ?) AS logouts
     * FROM user_sessions s JOIN users u ON u.id = s.user_id
     * WHERE (s.created_at &gt;= ? AND s.created_at &lt; ?)
     *    OR (s.revoked_reason = 'LOGOUT' AND s.revoked_at &gt;= ? AND s.revoked_at &lt; ?)
     * GROUP BY u.id, u.username, u.user_type
     * ORDER BY logins DESC, u.username
     * </pre>
     *
     * @param start first moment, included (e.g. 00:00 of a day in Phnom Penh)
     * @param end   last moment, excluded (00:00 of the next day)
     * @return one row per user
     */
    @Query(value = """
            SELECT u.id AS userId, u.username AS username, u.user_type AS userType,
                   count(*) FILTER (WHERE s.created_at >= :start AND s.created_at < :end) AS logins,
                   count(*) FILTER (WHERE s.revoked_reason = 'LOGOUT'
                                      AND s.revoked_at >= :start AND s.revoked_at < :end) AS logouts
            FROM user_sessions s JOIN users u ON u.id = s.user_id
            WHERE (s.created_at >= :start AND s.created_at < :end)
               OR (s.revoked_reason = 'LOGOUT' AND s.revoked_at >= :start AND s.revoked_at < :end)
            GROUP BY u.id, u.username, u.user_type
            ORDER BY logins DESC, u.username
            """, nativeQuery = true)
    List<UserLoginCount> countLoginsPerUser(Instant start, Instant end);

    /** One row of {@link #countLoginsPerUser}. */
    interface UserLoginCount {
        Long getUserId();
        String getUsername();
        String getUserType();
        long getLogins();
        long getLogouts();
    }
}
