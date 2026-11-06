package com.ums.security.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /auth/refresh: the refresh token from the last login or refresh. */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
