package com.ums.security.auth.application;

import com.ums.security.user.domain.User;
import java.time.Duration;
import java.util.Collection;

/** Makes the signed JWT tokens given out at login and refresh. Lifetimes come from application.yml. */
public interface JwtService {

    /**
     * Short-lived token sent with every request; carries the user's authorities.
     *
     * @param user        the logged-in user (id, username, type, language, must-change-password flag)
     * @param sessionId   id of the user_sessions row, put in the "sid" claim
     * @param authorities ROLE_* codes and permission codes
     * @return the signed token
     */
    String generateAccessToken(User user, Long sessionId, Collection<String> authorities);

    /**
     * Long-lived token used only at /auth/refresh; carries just the user and session ids.
     *
     * @param userId    the user
     * @param sessionId id of the user_sessions row
     * @return the signed token
     */
    String generateRefreshToken(Long userId, Long sessionId);

    /**
     * How long an access token stays valid (from the settings), e.g. for "expiresIn" in the login answer.
     *
     * @return the lifetime, e.g. 15 minutes
     */
    Duration getAccessTokenTtl();

    /**
     * How long a refresh token stays valid (from the settings).
     *
     * @return the lifetime
     */
    Duration getRefreshTokenTtl();
}
