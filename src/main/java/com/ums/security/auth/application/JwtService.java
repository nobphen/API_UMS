package com.ums.security.auth.application;

import com.ums.security.user.domain.User;
import java.time.Duration;
import java.util.Collection;

public interface JwtService {

    /** Short-lived token sent with every request; carries the user's authorities. */
    String generateAccessToken(User user, Long sessionId, Collection<String> authorities);

    /** Long-lived token used only at /auth/refresh; carries just the user and session ids. */
    String generateRefreshToken(Long userId, Long sessionId);

    Duration getAccessTokenTtl();

    Duration getRefreshTokenTtl();
}
