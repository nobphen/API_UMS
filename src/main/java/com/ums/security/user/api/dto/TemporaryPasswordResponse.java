package com.ums.security.user.api.dto;

/** Shown to the admin once, to print or read out; the user must change it at their next login. */
public record TemporaryPasswordResponse(String temporaryPassword) {
}
