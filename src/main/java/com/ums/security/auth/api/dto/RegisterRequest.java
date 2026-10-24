package com.ums.security.auth.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Self sign-up for applicants (prospective students); students and staff are created by the admin office. */
public record RegisterRequest(
        @NotBlank
        @Size(min = 4, max = 100)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{validation.username_chars}")
        String username,

        @NotBlank @Email @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        @NotBlank
        @Size(min = 8, max = 72)   // BCrypt only uses the first 72 bytes
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "{validation.password_strength}")
        String password,

        @NotBlank
        String confirmPassword,

        @NotNull @Valid
        DeviceInfo device) {
}
