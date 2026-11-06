package com.ums.security.auth.api.dto;

/** Tokens given after a login, refresh or password change; tokenType is always "Bearer". */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,                // access token lifetime in seconds
        long refreshExpiresIn,         // refresh token lifetime in seconds
        boolean mustChangePassword) {  // true → the app shows "Set your new password" before anything else
}
