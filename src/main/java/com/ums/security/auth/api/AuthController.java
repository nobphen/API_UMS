package com.ums.security.auth.api;

import com.ums.common.response.ApiResponse;
import com.ums.security.auth.api.dto.ChangePasswordRequest;
import com.ums.security.auth.api.dto.CurrentUserResponse;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.auth.api.dto.ForgotPasswordRequest;
import com.ums.security.auth.api.dto.LanguageRequest;
import com.ums.security.auth.api.dto.LoginRequest;
import com.ums.security.auth.api.dto.OtpLoginRequest;
import com.ums.security.auth.api.dto.OtpSendRequest;
import com.ums.security.auth.api.dto.QrLoginResponse;
import com.ums.security.auth.api.dto.RefreshTokenRequest;
import com.ums.security.auth.api.dto.RegisterRequest;
import com.ums.security.auth.api.dto.RegisterResponse;
import com.ums.security.auth.api.dto.ResendVerifyEmailRequest;
import com.ums.security.auth.api.dto.ResetPasswordRequest;
import com.ums.security.auth.api.dto.TokenResponse;
import com.ums.security.auth.api.dto.VerifyEmailRequest;
import com.ums.security.auth.application.AuthService;
import com.ums.security.auth.application.EmailVerificationService;
import com.ums.security.auth.application.OtpLoginService;
import com.ums.security.auth.application.PasswordResetService;
import com.ums.security.auth.application.QrLoginService;
import com.ums.security.user.application.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Sign-up, login (password, SMS code, QR code), tokens, logout and passwords. Most endpoints are public
 * (no token); logout, me, me/language, password/change and qr/approve need the access token.
 * Every answer is wrapped in ApiResponse: {"success": true, "data": ...}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final QrLoginService qrLoginService;
    private final OtpLoginService otpLoginService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;
    private final UserService userService;

    /// SIGN UP: 1. register (code sent by email) → 2. verify-email → 3. login

    /**
     * Applicants sign up here; no tokens until the email is verified. 201.
     * <pre>POST /api/v1/auth/register
     * {"username": "dara.sok", "email": "dara@gmail.com", "phone": "012345678",
     *  "password": "Dara1234", "confirmPassword": "Dara1234"}
     * → {"id": 42, "username": "dara.sok", "email": "dara@gmail.com"}</pre>
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /**
     * The 6-digit code from the sign-up email; the account becomes ACTIVE and can log in. 204.
     * <pre>POST /api/v1/auth/verify-email  {"email": "dara@gmail.com", "code": "123456"}</pre>
     */
    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.email(), request.code());
    }

    /**
     * "I didn't get the code". Always 202, whether or not the email has a pending account.
     * <pre>POST /api/v1/auth/verify-email/resend  {"email": "dara@gmail.com"}</pre>
     */
    @PostMapping("/verify-email/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendVerifyEmail(@Valid @RequestBody ResendVerifyEmailRequest request) {
        emailVerificationService.resendCode(request.email());
    }

    /**
     * Username, email or phone + password. mustChangePassword = true → call /password/change first.
     * <pre>POST /api/v1/auth/login
     * {"username": "ST2026001", "password": "Student123",
     *  "device": {"platform": "ANDROID", "deviceId": "a1b2c3", "deviceModel": "Galaxy A54"}, "rememberMe": true}
     * → {"accessToken": "eyJ...", "refreshToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 900,
     *    "refreshExpiresIn": ..., "mustChangePassword": false}</pre>
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return ApiResponse.ok(authService.login(request, clientIp(http), http.getHeader("User-Agent")));
    }

    /// SMS CODE LOGIN: 1. send a code to the phone → 2. log in with phone + code

    /**
     * Sends the login code by SMS. Always 204, whether or not the number has an account, so the answer reveals nothing.
     * <pre>POST /api/v1/auth/otp/send  {"phone": "012 345 678"}</pre>
     */
    @PostMapping("/otp/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendOtp(@Valid @RequestBody OtpSendRequest request) {
        otpLoginService.sendCode(request.phone());
    }

    /**
     * Phone + the code from the SMS → tokens, same answer as /login.
     * <pre>POST /api/v1/auth/otp/login
     * {"phone": "012 345 678", "code": "123456", "device": {"platform": "ANDROID", "deviceId": "a1b2c3"}}</pre>
     */
    @PostMapping("/otp/login")
    public ApiResponse<TokenResponse> loginWithOtp(@Valid @RequestBody OtpLoginRequest request, HttpServletRequest http) {
        return ApiResponse.ok(authService.loginWithOtp(request, clientIp(http), http.getHeader("User-Agent")));
    }

    /**
     * When the access token expires (401): new access + refresh token. Keep the NEW refresh token;
     * sending an old one again ends the session.
     * <pre>POST /api/v1/auth/refresh  {"refreshToken": "eyJ..."}</pre>
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    /**
     * Ends this device's session only. 204.
     * <pre>POST /api/v1/auth/logout   (Authorization: Bearer access token, no body)</pre>
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt);
    }

    /**
     * Who is logged in, read from the token (no database call). Works before the password is changed.
     * <pre>GET /api/v1/auth/me
     * → {"id": 42, "username": "ST2026001", "userType": "STUDENT", "sessionId": 7,
     *    "authorities": ["ROLE_STUDENT", ...], "mustChangePassword": false, "language": "km"}</pre>
     */
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(new CurrentUserResponse(
                Long.valueOf(Objects.requireNonNull(jwt.getSubject())),
                jwt.getClaimAsString("username"),
                jwt.getClaimAsString("user_type"),
                Long.valueOf(Objects.requireNonNull(jwt.getClaimAsString("sid"))),
                jwt.getClaimAsStringList("authorities"),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean("pwd_change")),
                Objects.requireNonNullElse(jwt.getClaimAsString("lang"), "en")));
    }

    /**
     * The user switches between Khmer and English; notifications and emails follow it too. 204.
     * <pre>PUT /api/v1/auth/me/language  {"language": "km"}</pre>
     */
    @PutMapping("/me/language")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setLanguage(@Valid @RequestBody LanguageRequest request, @AuthenticationPrincipal Jwt jwt) {
        userService.setPreferredLanguage(Long.valueOf(Objects.requireNonNull(jwt.getSubject())), request.language());
    }

    /// PASSWORD: change (logged in) · forgot → reset (not logged in, code by email)

    /**
     * Returns new tokens for this device (use them from now on); every other device is logged out.
     * <pre>POST /api/v1/auth/password/change
     * {"currentPassword": "Ab3kPq9z", "newPassword": "Student123", "confirmPassword": "Student123"}</pre>
     */
    @PostMapping("/password/change")
    public ApiResponse<TokenResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(authService.changePassword(jwt, request));
    }

    /**
     * "Forgot password?": emails a 6-digit code. Always 202, whether or not the account exists.
     * <pre>POST /api/v1/auth/password/forgot  {"username": "ST2026001"}</pre>
     */
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request);
    }

    /**
     * The code from the email + the new password; also unlocks a locked account. 204.
     * <pre>POST /api/v1/auth/password/reset
     * {"username": "ST2026001", "code": "123456", "newPassword": "NewPass123", "confirmPassword": "NewPass123"}</pre>
     */
    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }

    /// QR LOGIN: 1. web asks for a code → 2. mobile approves → 3. web polls until it gets tokens

    /**
     * Web login page: a new QR code, valid 2 minutes. Draw qrId as a QR picture.
     * <pre>POST /api/v1/auth/qr  → {"qrId": "9f1c...", "expiresIn": 120}</pre>
     */
    @PostMapping("/qr")
    public ApiResponse<QrLoginResponse> createQr() {
        return ApiResponse.ok(qrLoginService.create());
    }

    /**
     * Mobile app (logged in) after scanning: lets the web page log in as this user. 204.
     * <pre>POST /api/v1/auth/qr/9f1c.../approve   (Authorization: Bearer the mobile access token, no body)</pre>
     */
    @PostMapping("/qr/{qrId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveQr(@PathVariable String qrId, @AuthenticationPrincipal Jwt jwt) {
        qrLoginService.approve(qrId, Long.valueOf(Objects.requireNonNull(jwt.getSubject())));
    }

    /**
     * The web page asks every 2 seconds: 202 Accepted while waiting for the mobile app, 200 with tokens once approved.
     * <pre>POST /api/v1/auth/qr/9f1c.../token  {"platform": "WEB", "deviceId": "web-123", "deviceModel": "Chrome on Windows"}</pre>
     */
    @PostMapping("/qr/{qrId}/token")
    public ResponseEntity<ApiResponse<TokenResponse>> qrToken(@PathVariable String qrId,
                                                              @Valid @RequestBody DeviceInfo device,
                                                              HttpServletRequest http) {
        return authService.loginWithQr(qrId, device, clientIp(http), http.getHeader("User-Agent"))
                .map(tokens -> ResponseEntity.ok(ApiResponse.ok(tokens)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    /** The caller's real IP: the first one in X-Forwarded-For when behind a proxy (e.g. Nginx), else the socket address. */
    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : http.getRemoteAddr();
    }
}
