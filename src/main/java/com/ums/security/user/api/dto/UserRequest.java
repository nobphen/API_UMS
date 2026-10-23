package com.ums.security.user.api.dto;

import com.ums.security.user.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** An admin creates or edits a login account; the password is never sent here (a temporary one is generated). */
public record UserRequest(
        @NotBlank @Size(max = 100) String username,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String phone,
        @NotNull User.UserType userType,
        User.Status status,       // empty = ACTIVE
        Integer sessionTtlDays,   // 7, 30, 90, 180 or 365; empty = 180

        @Pattern(regexp = "^(km|en)$", message = "must be km or en")
        String preferredLanguage) {   // empty = km
}
