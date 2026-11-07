package com.ums.security.auth.api.dto;

import com.ums.common.util.Emails;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body of POST /auth/verify-email/resend: the email used at /register, to get a new sign-up code. */
public record ResendVerifyEmailRequest(@NotBlank @Email(regexp = Emails.REGEX, message = "{validation.email}") String email) {
}
