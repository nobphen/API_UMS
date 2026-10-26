package com.ums.common.exception;

import com.ums.common.response.ApiResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.hibernate.query.sqm.UnknownPathException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Every error answer, in the caller's language (see LocaleConfig and i18n/messages*.properties), always as
 * {@link ApiResponse}: success=false, message, and for invalid fields an errors map (field → problem).
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /** PostgreSQL error codes: a CHECK rule, and a NOT NULL column left empty. */
    private static final String CHECK_VIOLATION = "23514";
    private static final String NOT_NULL_VIOLATION = "23502";

    /** Checks that mean "this field must be filled in": answered as "<Field> is required". */
    private static final Set<String> REQUIRED = Set.of("NotBlank", "NotNull", "NotEmpty");

    private final MessageSource messageSource;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(text(ex.getMessage(), ex.getArgs())));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(text(ex.getMessage(), ex.getArgs())));
    }

    /**
     * A database rule was broken. The services check the data first, so this is either two requests at the same
     * moment (unique, foreign key → 409) or a bug that let wrong data through (CHECK, NOT NULL → 400); both are logged.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String sqlState = sqlState(ex);
        log.warn("Database rule broken (SQL state {}): {}", sqlState, ex.getMostSpecificCause().getMessage());
        if (CHECK_VIOLATION.equals(sqlState) || NOT_NULL_VIOLATION.equals(sqlState)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(text("error.validation")));
        }
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

    /**
     * @Valid failed on a request body: every invalid field, with its first problem. The problem text is already
     * translated by the validator; a nested field is named with dots, e.g. device.platform.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (REQUIRED.contains(error.getCode())) {
                errors.put(error.getField(), required(error.getField()));   // "is required" beats any other problem
            } else {
                errors.putIfAbsent(error.getField(), error.getDefaultMessage());
            }
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiResponse.error(text("error.validation"), errors));
    }

    /** A check on a path variable or query parameter failed, e.g. @Min(1) on ?size=. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleParameterValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors()
                    .forEach(error -> errors.putIfAbsent(name != null ? name : "value", error.getDefaultMessage()));
        });
        return ResponseEntity.badRequest().body(ApiResponse.error(text("error.validation"), errors));
    }

    /** /students/abc where a number is expected, or ?status=WRONG for an enum. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(text("error.validation"),
                Map.of(ex.getName(), text("error.invalid_value"))));
    }

    /**
     * ?sort= names a field that does not exist (e.g. sort=nothing,asc): Spring Data or Hibernate can't build the
     * ORDER BY. Other wrong uses of the data layer are bugs (500).
     */
    @ExceptionHandler({PropertyReferenceException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadSort(RuntimeException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof PropertyReferenceException || cause instanceof UnknownPathException) {
                return ResponseEntity.badRequest().body(ApiResponse.error(text("error.validation"),
                        Map.of("sort", text("error.invalid_value"))));
            }
        }
        return handleUnexpected(ex);
    }

    /** A required query parameter is missing, e.g. ?page= on an API that needs it. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(text("error.validation"),
                Map.of(ex.getParameterName(), required(ex.getParameterName()))));
    }

    /**
     * The body could not be read. A wrong value in a field ("abc" for a number, "PHONE" for an enum, a wrong date)
     * names the field, e.g. device.platform; no body or broken JSON gets a general message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        String field = wrongField(ex);
        if (field != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(text("error.validation"),
                    Map.of(field, text("error.invalid_value"))));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(text("error.bad_request_body")));
    }

    /** e.g. DELETE on an API that only has GET. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error(text("error.method_not_allowed")));
    }

    /** No API at this URL. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoEndpoint(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(text("error.endpoint_not_found")));
    }

    /**
     * Anything else. Spring's own HTTP errors (e.g. 415 wrong Content-Type) keep their status and short title;
     * the rest is a bug: logged with its stack trace, and the caller gets a general message, never the details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse response && !response.getStatusCode().is5xxServerError()) {
            return ResponseEntity.status(response.getStatusCode()).body(ApiResponse.error(response.getBody().getTitle()));
        }
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(text("error.internal")));
    }

    /**
     * The JSON field holding a wrong value, as device.platform or addresses[0].type; null when the JSON itself is
     * broken or the whole body has the wrong shape (e.g. a list instead of an object).
     */
    private static String wrongField(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof MismatchedInputException || cause instanceof InputCoercionException) {
                StringBuilder path = new StringBuilder();
                for (JacksonException.Reference reference : ((JacksonException) cause).getPath()) {
                    if (reference.getPropertyName() != null) {
                        if (!path.isEmpty()) path.append('.');
                        path.append(reference.getPropertyName());
                    } else if (reference.getIndex() >= 0) {
                        path.append('[').append(reference.getIndex()).append(']');
                    }
                }
                return path.isEmpty() ? null : path.toString();
            }
        }
        return null;
    }

    /** "Username is required" / "ឈ្មោះអ្នកប្រើ ត្រូវតែបំពេញ" for this field. */
    private String required(String field) {
        return text("validation.required", label(field));
    }

    /**
     * The field's name for people: from field.<name> in i18n/messages*.properties, else made from the Java name
     * (studentCode → Student code, programId → Program ID). For device.platform or addresses[0].type the last part counts.
     */
    private String label(String field) {
        String name = field.substring(field.lastIndexOf('.') + 1).replaceAll("\\[\\d+]", "");
        return messageSource.getMessage("field." + name, null, humanize(name), LocaleContextHolder.getLocale());
    }

    private static String humanize(String name) {
        String words = name.replaceAll("Id$", " ID").replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase();
        words = words.replace(" id", " ID");
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    /** PostgreSQL's error code (SQL state) inside the exception, e.g. 23514; null when there is none. */
    private static String sqlState(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null) return sql.getSQLState();
        }
        return null;
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
