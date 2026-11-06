package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body of POST /auth/verify-email/resend: the email used at /register, to get a new sign-up code. */
public record ResendVerifyEmailRequest(@NotBlank @Email String email) {
}
