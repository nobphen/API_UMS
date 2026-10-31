package com.ums.common.response;

import java.util.Map;
import org.springframework.data.domain.Page;

/**
 * The one shape of every API answer, success or error:
 * <pre>
 * { "status": 201, "success": true,  "message": "Created successfully", "data": {...}, "errors": null }
 * { "status": 400, "success": false, "message": "Some fields are invalid", "data": null, "errors": {"username": "must not be blank"} }
 * </pre>
 * {@code status} is the HTTP status code, filled by {@link ApiResponseAdvice} from the real response, so the two never
 * differ. {@code message} may be a key from i18n/messages*.properties (e.g. success.created): the advice writes it in
 * the caller's language. {@code errors} is only set when fields are invalid: field name → problem.
 */
public record ApiResponse<T>(int status, boolean success, String message, T data, Map<String, String> errors) {

    /** GET one record, a list, or any other action (200). */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, true, "success.ok", data, null);
    }

    /** Any success with your own message key, e.g. ("success.password_changed", null). */
    public static <T> ApiResponse<T> ok(String messageKey, T data) {
        return new ApiResponse<>(0, true, messageKey, data, null);
    }

    /** POST that made a new record; use with @ResponseStatus(HttpStatus.CREATED) (201). */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(0, true, "success.created", data, null);
    }

    /** PUT / PATCH (200). */
    public static <T> ApiResponse<T> updated(T data) {
        return new ApiResponse<>(0, true, "success.updated", data, null);
    }

    /** DELETE (200); data is null. */
    public static ApiResponse<Void> deleted() {
        return new ApiResponse<>(0, true, "success.deleted", null, null);
    }

    /** GET a page of records (200): data is a {@link PageResponse}. */
    public static <T> ApiResponse<PageResponse<T>> page(Page<T> page) {
        return new ApiResponse<>(0, true, "success.ok", PageResponse.of(page), null);
    }

    /** GET records loaded from an id, e.g. chat messages (200): data is a {@link CursorResponse}. */
    public static <T> ApiResponse<CursorResponse<T>> cursor(CursorResponse<T> cursor) {
        return new ApiResponse<>(0, true, "success.ok", cursor, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(0, false, message, null, null);
    }

    /** Invalid fields (400): one problem per field. */
    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(0, false, message, null, errors);
    }

    /** The same answer with the HTTP status and the final message text. */
    public ApiResponse<T> with(int status, String message) {
        return new ApiResponse<>(status, success, message, data, errors);
    }
}
