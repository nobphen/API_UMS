package com.ums.security.user.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.security.RequirePermission;
import com.ums.security.SecurityPermissions;
import com.ums.security.user.api.dto.TemporaryPasswordResponse;
import com.ums.security.user.api.dto.UserRoleRequest;
import com.ums.security.user.api.dto.UserRoleResponse;
import com.ums.security.user.application.UserService;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** For a user who can't use "Forgot password?" (no email): the office hands over a new temporary password. */
    @PostMapping("/{userId}/reset-password")
    @RequirePermission(SecurityPermissions.USER_UPDATE)
    public ApiResponse<TemporaryPasswordResponse> resetPassword(@PathVariable Long userId, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(new TemporaryPasswordResponse(userService.resetPassword(userId, adminId(jwt))));
    }

    @PostMapping("/{userId}/lock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(SecurityPermissions.USER_LOCK)
    public void lock(@PathVariable Long userId, @AuthenticationPrincipal Jwt jwt) {
        userService.lock(userId, adminId(jwt));
    }

    @PostMapping("/{userId}/unlock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(SecurityPermissions.USER_LOCK)
    public void unlock(@PathVariable Long userId) {
        userService.unlock(userId);
    }

    /// ROLES OF A USER ----------------------------------------------

    @GetMapping("/{userId}/roles")
    @RequirePermission(SecurityPermissions.ROLE_VIEW)
    public ApiResponse<List<UserRoleResponse>> roles(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getRoles(userId));
    }

    @PostMapping("/{userId}/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(SecurityPermissions.ROLE_MANAGE)
    public ApiResponse<UserRoleResponse> assignRole(@PathVariable Long userId, @Valid @RequestBody UserRoleRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(userService.assignRole(userId, request, authorities(jwt)));
    }

    @DeleteMapping("/{userId}/roles/{userRoleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(SecurityPermissions.ROLE_MANAGE)
    public void removeRole(@PathVariable Long userId, @PathVariable Long userRoleId, @AuthenticationPrincipal Jwt jwt) {
        userService.removeRole(userId, userRoleId, authorities(jwt));
    }

    private static Set<String> authorities(Jwt jwt) {
        List<String> list = jwt.getClaimAsStringList("authorities");
        return list == null ? Set.of() : new HashSet<>(list);
    }

    private static Long adminId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }
}
