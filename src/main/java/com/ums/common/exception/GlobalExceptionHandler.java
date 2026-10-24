package com.ums.common.exception;

import com.ums.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Every error answer, in the caller's language (see LocaleConfig and i18n/messages*.properties). */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(text(ex.getMessage(), ex.getArgs())));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(text(ex.getMessage(), ex.getArgs())));
    }

    /** A unique or foreign-key rule in the database was broken, e.g. two sign-ups with the same username at once. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(text("error.conflict")));
    }

    /** Wrong password, locked account, invalid or reused refresh token. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(text(authKey(ex))));
    }

    /** Logged in, but missing the permission (@RequirePermission). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(text("error.forbidden")));
    }

    /** @Valid failed on a request body. The constraint's message is already translated by the validator. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse(text("error.validation"));
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    /** The message for this key in the request's language; the key itself when no translation exists yet. */
    private String text(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    /** Our own auth errors carry a key; Spring's own ones ("Bad credentials", "User account is locked") are mapped. */
    private String authKey(AuthenticationException ex) {
        String message = ex.getMessage();
        if (message != null && message.startsWith("auth.")) return message;
        if (ex instanceof LockedException) return "auth.account_locked";
        if (ex instanceof DisabledException) return "auth.account_disabled";
        if (ex instanceof BadCredentialsException) return "auth.bad_credentials";
        return "auth.invalid_token";
    }
}
