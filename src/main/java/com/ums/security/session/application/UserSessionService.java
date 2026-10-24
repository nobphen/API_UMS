package com.ums.security.session.application;

import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.session.api.dto.LoginStatsResponse;
import com.ums.security.session.api.dto.UserLoginStatsResponse;
import com.ums.security.session.domain.UserSession;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public interface UserSessionService {

    /** Opens a session for a device; an active session on the same device is revoked as REPLACED. */
    UserSession create(Long userId, DeviceInfo device, String ipAddress, String userAgent,
                       UserSession.LoginMethod loginMethod, boolean rememberMe, Duration ttl);

    /** Stores the hash of the newly issued refresh token and extends the session. */
    void updateRefreshToken(UserSession session, String refreshToken, Duration ttl);

    /**
     * Returns the session if it is active and the token is its current refresh token.
     * A token that was already replaced means it was stolen and reused: the session is revoked as TOKEN_REUSE.
     */
    UserSession getActiveSession(Long sessionId, String refreshToken);

    /** The session the access token belongs to; fails if it was logged out or has expired. */
    UserSession getActiveSession(Long sessionId);

    /** The user's logged-in devices, most recently used first. */
    List<UserSession> getActiveSessions(Long userId);

    void revoke(Long sessionId, UserSession.RevokedReason reason, Long revokedBy);

    /** The user ends one of their own sessions from the "Devices" screen. */
    void revokeOwn(Long userId, Long sessionId);

    /** An admin ends one session of a user, e.g. the device of someone who got into the account. */
    void revokeByAdmin(Long userId, Long sessionId, Long adminId);

    /** Ends every session of the user except keepSessionId (null ends all of them); returns how many were ended. */
    int revokeAll(Long userId, UserSession.RevokedReason reason, Long revokedBy, Long keepSessionId);

    /** Cleanup job: ends expired sessions and ones unused for longer than the user's session_ttl_days. */
    int revokeExpiredAndInactive();

    /** True for a revoked session, so its access tokens are refused before they expire (checked in Redis). */
    boolean isRevoked(Long sessionId);

    /**
     * Logins and logouts per day, Cambodia time. Leave out to for today and from for the 7 days up to to.
     *
     * @param from first day, or null
     * @param to   last day (included), or null
     * @return one row per day, oldest first, at most 366 days
     */
    List<LoginStatsResponse> loginStats(LocalDate from, LocalDate to);

    /**
     * Logins and logouts of each user on one day, Cambodia time, most logins first.
     *
     * @param date the day, or null for today
     * @return one row per user who logged in or out that day
     */
    List<UserLoginStatsResponse> loginStatsByUser(LocalDate date);
}
