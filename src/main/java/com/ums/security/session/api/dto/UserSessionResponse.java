package com.ums.security.session.api.dto;

import com.ums.security.session.domain.UserSession;
import java.time.Instant;

/** One row on the "Devices" screen. */
public record UserSessionResponse(
        Long id,
        String deviceModel,
        UserSession.Platform platform,
        String osVersion,
        String appName,
        String appVersion,
        String ipAddress,
        String country,
        String city,
        UserSession.LoginMethod loginMethod,
        Instant createdAt,
        Instant lastActiveAt,
        boolean current) {   // the session making this request ("This device")
}
