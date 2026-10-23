package com.ums.security.auth.api.dto;

/** Shown on the web login page as a QR code; the logged-in mobile app scans it and approves. */
public record QrLoginResponse(String qrId, long expiresIn) {
}
