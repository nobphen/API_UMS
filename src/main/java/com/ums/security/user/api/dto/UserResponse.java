package com.ums.security.user.api.dto;

import com.ums.common.response.PhoneDisplay;
import com.ums.security.user.domain.User;
import java.time.OffsetDateTime;

/** One login account as sent to the client (never the password hash); the phone is shown in local format. */
public record UserResponse(
        Long id,
        String username,
        String email,
        @PhoneDisplay String phone,
        User.UserType userType,
        User.Status status,
        int failedLoginCount,
        OffsetDateTime lastLoginAt,
        int sessionTtlDays,
        boolean mustChangePassword,
        String preferredLanguage) {
}
