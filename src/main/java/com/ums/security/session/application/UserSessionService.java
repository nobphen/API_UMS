package com.ums.security.session.application;

import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.session.domain.UserSession;
import java.time.Duration;
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

    /** Ends every session of the user except keepSessionId (null ends all of them); returns how many were ended. */
    int revokeAll(Long userId, UserSession.RevokedReason reason, Long revokedBy, Long keepSessionId);

    /** Cleanup job: ends expired sessions and ones unused for longer than the user's session_ttl_days. */
    int revokeExpiredAndInactive();

    /** True for a revoked session, so its access tokens are refused before they expire (checked in Redis). */
    boolean isRevoked(Long sessionId);
}
