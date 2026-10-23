package com.ums.security.auth.api;

import com.ums.common.response.ApiResponse;
import com.ums.security.auth.api.dto.ChangePasswordRequest;
import com.ums.security.auth.api.dto.CurrentUserResponse;
import com.ums.security.auth.api.dto.DeviceInfo;
import com.ums.security.auth.api.dto.ForgotPasswordRequest;
import com.ums.security.auth.api.dto.LanguageRequest;
import com.ums.security.auth.api.dto.LoginRequest;
import com.ums.security.auth.api.dto.QrLoginResponse;
import com.ums.security.auth.api.dto.RefreshTokenRequest;
import com.ums.security.auth.api.dto.RegisterRequest;
import com.ums.security.auth.api.dto.ResetPasswordRequest;
import com.ums.security.auth.api.dto.TokenResponse;
import com.ums.security.auth.application.AuthService;
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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final QrLoginService qrLoginService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;

    /** Applicants sign up here; the new account is logged in straight away. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return ApiResponse.ok(authService.register(request, clientIp(http), http.getHeader("User-Agent")));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return ApiResponse.ok(authService.login(request, clientIp(http), http.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(new CurrentUserResponse(
                Long.valueOf(Objects.requireNonNull(jwt.getSubject())),
                jwt.getClaimAsString("username"),
                jwt.getClaimAsString("user_type"),
                Long.valueOf(Objects.requireNonNull(jwt.getClaimAsString("sid"))),
                jwt.getClaimAsStringList("authorities"),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean("pwd_change")),
                Objects.requireNonNullElse(jwt.getClaimAsString("lang"), "km")));
    }

    /** The user switches between Khmer and English; notifications and emails follow it too. */
    @PutMapping("/me/language")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setLanguage(@Valid @RequestBody LanguageRequest request, @AuthenticationPrincipal Jwt jwt) {
        userService.setPreferredLanguage(Long.valueOf(Objects.requireNonNull(jwt.getSubject())), request.language());
    }

    /// PASSWORD: change (logged in) · forgot → reset (not logged in, code by email)

    /** Returns new tokens for this device; every other device is logged out. */
    @PostMapping("/password/change")
    public ApiResponse<TokenResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(authService.changePassword(jwt, request));
    }

    /** Always 202, whether or not the account exists. */
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }

    /// QR LOGIN: 1. web asks for a code → 2. mobile approves → 3. web polls until it gets tokens

    @PostMapping("/qr")
    public ApiResponse<QrLoginResponse> createQr() {
        return ApiResponse.ok(qrLoginService.create());
    }

    @PostMapping("/qr/{qrId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveQr(@PathVariable String qrId, @AuthenticationPrincipal Jwt jwt) {
        qrLoginService.approve(qrId, Long.valueOf(Objects.requireNonNull(jwt.getSubject())));
    }

    /** 202 Accepted while waiting for the mobile app, 200 with tokens once approved. */
    @PostMapping("/qr/{qrId}/token")
    public ResponseEntity<ApiResponse<TokenResponse>> qrToken(@PathVariable String qrId,
                                                              @Valid @RequestBody DeviceInfo device,
                                                              HttpServletRequest http) {
        return authService.loginWithQr(qrId, device, clientIp(http), http.getHeader("User-Agent"))
                .map(tokens -> ResponseEntity.ok(ApiResponse.ok(tokens)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    private static String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : http.getRemoteAddr();
    }
}
