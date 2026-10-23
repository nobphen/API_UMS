package com.ums.security.auth.application;

import com.ums.security.auth.api.dto.ChangePasswordRequest;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.auth.api.dto.LoginRequest;
import com.ums.security.auth.api.dto.RefreshTokenRequest;
import com.ums.security.auth.api.dto.RegisterRequest;
import com.ums.security.auth.api.dto.TokenResponse;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {

    /** Creates an APPLICANT account and logs it in straight away. */
    TokenResponse register(RegisterRequest request, String ipAddress, String userAgent);

    /** Username (or email) + password → new session with access and refresh tokens. */
    TokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    /** Valid refresh token → new access token and a new refresh token (the old one stops working). */
    TokenResponse refresh(RefreshTokenRequest request);

    /** Ends the session the access token belongs to. */
    void logout(Jwt accessToken);

    /** Tokens for the web page once the mobile app has approved the QR code; empty while still pending. */
    Optional<TokenResponse> loginWithQr(String qrId, DeviceInfo device, String ipAddress, String userAgent);

    /**
     * The logged-in user sets a new password (also clears a temporary one). Every other session ends;
     * this one gets new tokens, because the old access token still says the password must be changed.
     */
    TokenResponse changePassword(Jwt accessToken, ChangePasswordRequest request);
}
