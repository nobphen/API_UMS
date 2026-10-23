package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank String username) {   // username, student ID or email
}
