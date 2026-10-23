package com.ums.security.user.api.dto;

import com.ums.security.user.domain.User;
import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String phone,
        User.UserType userType,
        User.Status status,
        int failedLoginCount,
        OffsetDateTime lastLoginAt,
        int sessionTtlDays,
        boolean mustChangePassword,
        String preferredLanguage) {
}
