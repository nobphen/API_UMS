package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank
        String username,   // same value as sent to /password/forgot

        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "must be the 6-digit code")
        String code,

        @NotBlank
        @Size(min = 8, max = 72)   // BCrypt only uses the first 72 bytes
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain at least one letter and one digit")
        String newPassword,

        @NotBlank
        String confirmPassword) {
}
