package com.ums.security.filter;

import com.ums.security.session.application.UserSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Runs right after the Bearer token is verified. Refuses refresh tokens used as access tokens, and
 * access tokens of a session that was logged out or ended by an admin (via the Redis revoke list),
 * and everything except a password change while the user still has a temporary password.
 */
@Component
@RequiredArgsConstructor
public class SessionActivityFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_BEFORE_PASSWORD_CHANGE = Set.of(
            "/api/v1/auth/password/change", "/api/v1/auth/logout", "/api/v1/auth/me");

    private final UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            // Access and refresh tokens share one key, so a refresh token would also pass the signature check
            if (!"access".equals(jwtAuth.getToken().getClaimAsString("typ"))) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Use an access token");
                return;
            }
            String sid = jwtAuth.getToken().getClaimAsString("sid");
            if (sid != null && userSessionService.isRevoked(Long.valueOf(sid))) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session has ended, please log in again");
                return;
            }
            // Temporary password (new account or admin reset): nothing else works until it is changed
            if (Boolean.TRUE.equals(jwtAuth.getToken().getClaimAsBoolean("pwd_change"))
                    && !ALLOWED_BEFORE_PASSWORD_CHANGE.contains(request.getRequestURI())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Change your password first");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
