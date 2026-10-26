package com.ums.security.filter;

import com.ums.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import tools.jackson.databind.json.JsonMapper;

/**
 * 401 and 403 found by the security filters, before any controller runs (no token, expired token, no access),
 * as the same {@link ApiResponse} JSON as every other error. GlobalExceptionHandler can't answer these:
 * they happen before Spring MVC.
 */
@Component
@NamedInterface("security-error-handler")   // used by other modules
@RequiredArgsConstructor
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final JsonMapper jsonMapper;

    /** No token: please log in. A token that is wrong or expired: log in again. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        String key = ex instanceof InvalidBearerTokenException ? "auth.invalid_token" : "auth.login_required";
        response.setHeader("WWW-Authenticate", "Bearer");
        write(request, response, HttpStatus.UNAUTHORIZED, key);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "error.forbidden");
    }

    /**
     * Writes the error as ApiResponse JSON in the caller's language and ends the request.
     * Also used by SessionActivityFilter, so every error from the security filters has the same shape.
     */
    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String key)
            throws IOException {
        String message = messageSource.getMessage(key, null, key, localeResolver.resolveLocale(request));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), ApiResponse.error(message).with(status.value(), message));
    }
}
