package com.ums.security.auth.api.dto;

/** The new account; no tokens: the applicant verifies the email first, then logs in. */
public record RegisterResponse(Long id, String username, String email) {
}
