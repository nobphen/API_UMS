package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(
        @NotBlank @Email
        String email,   // same email as sent to /register

        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "{validation.reset_code}")
        String code) {
}
