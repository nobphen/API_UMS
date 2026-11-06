package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /auth/password/forgot: who forgot the password; a reset code is emailed to that account. */
public record ForgotPasswordRequest(@NotBlank String username) {   // username (student code), email or phone number
}
