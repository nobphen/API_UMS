package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank
        String username,   // same value as sent to /password/forgot

        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "{validation.reset_code}")
        String code,

        @NotBlank
        @Size(min = 8, max = 72)   // BCrypt only uses the first 72 bytes
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.password_strength}")
        String newPassword,

        @NotBlank
        String confirmPassword) {
}
