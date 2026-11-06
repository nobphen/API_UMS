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

/**
 * What an admin does to a login account: new temporary password, lock / unlock, and its roles.
 * {userId} is users.id (not students.id): SELECT id FROM users WHERE username = 'ST2026001'.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * For a user who can't use "Forgot password?" (no email): the office hands over a new temporary password.
     * The user's sessions end; they must change it at the next login.
     * <pre>POST /api/v1/users/42/reset-password   (no body)  → {"temporaryPassword": "Ab3kPq9z"}</pre>
     */
    @PostMapping("/{userId}/reset-password")
    @RequirePermission(SecurityPermissions.USER_UPDATE)
    public ApiResponse<TemporaryPasswordResponse> resetPassword(@PathVariable Long userId, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(new TemporaryPasswordResponse(userService.resetPassword(userId, adminId(jwt))));
    }

    /**
     * Blocks the login and ends all its sessions; you can't lock yourself. 204.
     * <pre>POST /api/v1/users/42/lock   (no body)</pre>
     */
    @PostMapping("/{userId}/lock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(SecurityPermissions.USER_LOCK)
    public void lock(@PathVariable Long userId, @AuthenticationPrincipal Jwt jwt) {
        userService.lock(userId, adminId(jwt));
    }

    /**
     * LOCKED → ACTIVE and the wrong-password count back to 0 (e.g. after 5 wrong passwords). 204.
     * <pre>POST /api/v1/users/42/unlock   (no body)</pre>
     */
    @PostMapping("/{userId}/unlock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(SecurityPermissions.USER_LOCK)
    public void unlock(@PathVariable Long userId) {
        userService.unlock(userId);
    }

    /// ROLES OF A USER ----------------------------------------------

    /**
     * The user's roles with scope and dates, oldest first.
     * <pre>GET /api/v1/users/42/roles
     * → [{"id": 5, "roleCode": "STUDENT", "roleName": "Student", "scopeType": "GLOBAL", "scopeId": null,
     *     "validFrom": null, "validTo": null}]</pre>
     */
    @GetMapping("/{userId}/roles")
    @RequirePermission(SecurityPermissions.ROLE_VIEW)
    public ApiResponse<List<UserRoleResponse>> roles(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getRoles(userId));
    }

    /**
     * Gives a role, for the whole university or one faculty / department / program / class section. 201.
     * The user gets it at their next token refresh (at most 15 minutes).
     * <pre>POST /api/v1/users/42/roles
     * {"roleCode": "DEAN", "scopeType": "FACULTY", "scopeId": 3, "validFrom": "2026-11-01", "validTo": "2028-10-31"}</pre>
     */
    @PostMapping("/{userId}/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(SecurityPermissions.ROLE_MANAGE)
    public ApiResponse<UserRoleResponse> assignRole(@PathVariable Long userId, @Valid @RequestBody UserRoleRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(userService.assignRole(userId, request, authorities(jwt)));
    }

    /**
     * Takes a role away; {userRoleId} is the "id" from GET roles, not the role's id. The last SUPER_ADMIN stays. 204.
     * <pre>DELETE /api/v1/users/42/roles/5</pre>
     */
    @DeleteMapping("/{userId}/roles/{userRoleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(SecurityPermissions.ROLE_MANAGE)
    public void removeRole(@PathVariable Long userId, @PathVariable Long userRoleId, @AuthenticationPrincipal Jwt jwt) {
        userService.removeRole(userId, userRoleId, authorities(jwt));
    }

    /** The caller's own ROLE_* and permission codes, from the "authorities" claim of the token. */
    private static Set<String> authorities(Jwt jwt) {
        List<String> list = jwt.getClaimAsStringList("authorities");
        return list == null ? Set.of() : new HashSet<>(list);
    }

    /** The logged-in admin's users.id (the token's subject). */
    private static Long adminId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }
}
