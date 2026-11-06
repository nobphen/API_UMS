package com.ums.security.auth.application;

import com.ums.security.auth.api.dto.ChangePasswordRequest;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.auth.api.dto.LoginRequest;
import com.ums.security.auth.api.dto.OtpLoginRequest;
import com.ums.security.auth.api.dto.RefreshTokenRequest;
import com.ums.security.auth.api.dto.RegisterRequest;
import com.ums.security.auth.api.dto.RegisterResponse;
import com.ums.security.auth.api.dto.TokenResponse;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Sign-up, every way to log in (password, SMS code, QR code), token refresh, logout and password change.
 * Every login opens a session (one row in user_sessions per device) and returns an access + refresh token.
 * Used by AuthController.
 */
public interface AuthService {

    /**
     * Creates a PENDING APPLICANT account and emails a code; the applicant verifies it, then logs in.
     *
     * @param request username, email, phone (optional), password and confirmPassword
     * @return the new account (id, username, email); no tokens yet
     * @throws com.ums.common.exception.BusinessException error.password.mismatch when the two passwords differ;
     *                                                    error.user.username_taken / email_taken / phone_taken
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Username, email or phone number + password → new session with access and refresh tokens.
     * 5 wrong passwords in a row lock the account.
     *
     * @param request   username (or email or phone), password, device, rememberMe
     * @param ipAddress the caller's IP, saved on the session
     * @param userAgent the browser or app, saved on the session
     * @return the tokens; mustChangePassword = true when the password is temporary
     * @throws org.springframework.security.core.AuthenticationException auth.bad_credentials (wrong login or password);
     *                                                                   auth.account_locked / account_disabled;
     *                                                                   auth.account_pending (email not verified yet)
     */
    TokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Phone number + the code sent by SMS (see OtpLoginService.sendCode) → new session, as for a password login.
     *
     * @param request   phone, the 6-digit code, device, rememberMe
     * @param ipAddress the caller's IP, saved on the session
     * @param userAgent the browser or app, saved on the session
     * @return the tokens
     * @throws com.ums.common.exception.BusinessException error.otp.invalid_code / error.otp.too_many_attempts
     * @throws org.springframework.security.core.AuthenticationException auth.account_locked / account_disabled / account_pending
     */
    TokenResponse loginWithOtp(OtpLoginRequest request, String ipAddress, String userAgent);

    /**
     * Valid refresh token → new access token and a new refresh token (the old one stops working).
     * Using an old refresh token a second time ends the whole session (someone may have stolen it).
     *
     * @param request the refresh token
     * @return new tokens for the same session
     * @throws org.springframework.security.core.AuthenticationException auth.not_refresh_token (an access token was sent);
     *                                                                   auth.session_ended / auth.refresh_reused;
     *                                                                   auth.account_locked / account_disabled
     */
    TokenResponse refresh(RefreshTokenRequest request);

    /**
     * Ends the session the access token belongs to (this device only; other devices stay logged in).
     *
     * @param accessToken the caller's access token (its "sid" claim is the session id)
     */
    void logout(Jwt accessToken);

    /**
     * Tokens for the web page once the mobile app has approved the QR code; empty while still pending.
     * The web page calls it again every 2 seconds or so until it gets the tokens.
     *
     * @param qrId      id from QrLoginService.create
     * @param device    the web browser's device info
     * @param ipAddress the caller's IP, saved on the session
     * @param userAgent the browser, saved on the session
     * @return the tokens once approved; empty while waiting
     * @throws com.ums.common.exception.BusinessException error.qr.expired / error.qr.used
     */
    Optional<TokenResponse> loginWithQr(String qrId, DeviceInfo device, String ipAddress, String userAgent);

    /**
     * The logged-in user sets a new password (also clears a temporary one). Every other session ends;
     * this one gets new tokens, because the old access token still says the password must be changed.
     *
     * @param accessToken the caller's access token
     * @param request     currentPassword, newPassword, confirmPassword
     * @return new tokens for this device (mustChangePassword = false)
     * @throws com.ums.common.exception.BusinessException error.password.current_wrong;
     *                                                    error.password.mismatch;
     *                                                    error.password.same_as_current
     */
    TokenResponse changePassword(Jwt accessToken, ChangePasswordRequest request);
}
