package com.ums.security.filter;

import com.ums.security.session.application.UserSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Runs right after the Bearer token is verified. Refuses refresh tokens used as access tokens, and
 * access tokens of a session that was logged out or ended by an admin (via the Redis revoke list),
 * and everything except a password change while the user still has a temporary password.
 */
@NamedInterface("session-filter")   // used by other modules
@Component
@RequiredArgsConstructor
public class SessionActivityFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_BEFORE_PASSWORD_CHANGE = Set.of(
            "/api/v1/auth/password/change", "/api/v1/auth/logout", "/api/v1/auth/me");

    private final UserSessionService userSessionService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            // Access and refresh tokens share one key, so a refresh token would also pass the signature check
            if (!"access".equals(jwtAuth.getToken().getClaimAsString("typ"))) {
                reject(request, response, HttpServletResponse.SC_UNAUTHORIZED, "auth.use_access_token");
                return;
            }
            String sid = jwtAuth.getToken().getClaimAsString("sid");
            if (sid != null && userSessionService.isRevoked(Long.valueOf(sid))) {
                reject(request, response, HttpServletResponse.SC_UNAUTHORIZED, "auth.session_ended");
                return;
            }
            // Temporary password (new account or admin reset): nothing else works until it is changed
            if (Boolean.TRUE.equals(jwtAuth.getToken().getClaimAsBoolean("pwd_change"))
                    && !ALLOWED_BEFORE_PASSWORD_CHANGE.contains(request.getRequestURI())) {
                reject(request, response, HttpServletResponse.SC_FORBIDDEN, "auth.change_password_first");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /** Answers like every other API error (ApiResponse JSON), in the caller's language, and ends the request. */
    private void reject(HttpServletRequest request, HttpServletResponse response, int status, String key) throws IOException {
        String message = messageSource.getMessage(key, null, key, localeResolver.resolveLocale(request));   // before the context is cleared: it may carry the user's language
        SecurityContextHolder.clearContext();
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\"data\":null}");
    }
}
