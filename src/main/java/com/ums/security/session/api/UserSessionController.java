package com.ums.security.session.api;

import com.ums.common.response.ApiResponse;
import com.ums.security.session.api.dto.UserSessionResponse;
import com.ums.security.session.application.UserSessionService;
import com.ums.security.session.application.mapper.UserSessionMapper;
import com.ums.security.session.domain.UserSession;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-sessions")
@RequiredArgsConstructor
public class UserSessionController {

    private final UserSessionService userSessionService;
    private final UserSessionMapper userSessionMapper;

    /// MY DEVICES ---------------------------------------------------

    @GetMapping("/me")
    public ApiResponse<List<UserSessionResponse>> mySessions(@AuthenticationPrincipal Jwt jwt) {
        Long currentSessionId = sessionId(jwt);
        return ApiResponse.ok(userSessionService.getActiveSessions(userId(jwt)).stream()
                .map(s -> userSessionMapper.toResponse(s, currentSessionId))
                .toList());
    }

    @DeleteMapping("/me/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endMySession(@PathVariable Long sessionId, @AuthenticationPrincipal Jwt jwt) {
        userSessionService.revokeOwn(userId(jwt), sessionId);
    }

    /** "Log out all other devices": keeps only the session making this request. Returns how many were ended. */
    @PostMapping("/me/terminate-others")
    public ApiResponse<Integer> endMyOtherSessions(@AuthenticationPrincipal Jwt jwt) {
        Long userId = userId(jwt);
        return ApiResponse.ok(userSessionService.revokeAll(
                userId, UserSession.RevokedReason.TERMINATE_OTHERS, userId, sessionId(jwt)));
    }

    /// ADMIN: another user's devices (e.g. a student reports a stolen phone) ------

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).USER_VIEW)")
    public ApiResponse<List<UserSessionResponse>> userSessions(@PathVariable Long userId) {
        return ApiResponse.ok(userSessionService.getActiveSessions(userId).stream()
                .map(s -> userSessionMapper.toResponse(s, null))
                .toList());
    }

    @PostMapping("/users/{userId}/terminate-all")
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).USER_LOCK)")
    public ApiResponse<Integer> endAllUserSessions(@PathVariable Long userId, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(userSessionService.revokeAll(
                userId, UserSession.RevokedReason.ADMIN, userId(jwt), null));
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }

    private static Long sessionId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getClaimAsString("sid")));
    }
}
