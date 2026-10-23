package com.ums.security.auth.api.dto;

import com.ums.security.session.domain.UserSession;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The device the user logs in from; shown later in "active sessions". */
public record DeviceInfo(
        @Size(max = 100) String deviceId,      // mobile: generated on first launch; web: random id kept in localStorage
        @Size(max = 100) String deviceModel,   // iPhone 15, Chrome on Windows
        @NotNull UserSession.Platform platform,
        @Size(max = 50) String osVersion,
        @Size(max = 50) String appName,
        @Size(max = 20) String appVersion,
        @Size(max = 255) String fcmToken) {
}
