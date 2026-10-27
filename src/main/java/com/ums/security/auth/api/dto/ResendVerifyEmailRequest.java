package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerifyEmailRequest(@NotBlank @Email String email) {
}
