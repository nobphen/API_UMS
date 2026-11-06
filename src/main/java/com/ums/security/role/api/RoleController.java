package com.ums.security.role.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.security.RequirePermission;
import com.ums.security.SecurityPermissions;
import com.ums.security.role.api.dto.RoleRequest;
import com.ums.security.role.api.dto.RoleResponse;
import com.ums.security.role.api.dto.RoleUpdateRequest;
import com.ums.security.role.application.RoleService;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admins list, add, change and delete roles here. Reading needs ROLE_VIEW, changing needs ROLE_MANAGE.
 * Every answer is wrapped in ApiResponse: {"success": true, "data": ...}.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * All roles, system roles first, each with its permission codes and user count.
     * <pre>GET /api/v1/roles
     * → [{"id": 1, "code": "SUPER_ADMIN", "name": "Super admin", "system": true, "status": "ACTIVE",
     *     "permissions": ["ROLE_MANAGE", ...], "userCount": 1}, ...]</pre>
     */
    @GetMapping
    @RequirePermission(SecurityPermissions.ROLE_VIEW)
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.ok(roleService.list());
    }

    /**
     * One role; 404 when it does not exist.
     * <pre>GET /api/v1/roles/5  → {"id": 5, "code": "LIBRARIAN", "name": "Librarian", ...}</pre>
     */
    @GetMapping("/{id}")
    @RequirePermission(SecurityPermissions.ROLE_VIEW)
    public ApiResponse<RoleResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(roleService.get(id));
    }

    /**
     * Adds a role. The admin can only put in permissions they hold themselves. 201.
     * <pre>POST /api/v1/roles
     * {"code": "LIBRARIAN", "name": "Librarian", "nameKh": "...", "description": "Runs the library",
     *  "permissions": ["BOOK_MANAGE"]}
     * → {"id": 5, "code": "LIBRARIAN", "system": false, "status": "ACTIVE", "userCount": 0, ...}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(SecurityPermissions.ROLE_MANAGE)
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(roleService.create(request, authorities(jwt)));
    }

    /**
     * Changes names, status and (when sent) permissions; the code can't change.
     * <pre>PUT /api/v1/roles/5
     * {"name": "Librarian", "status": "INACTIVE", "permissions": ["BOOK_MANAGE", "BOOK_VIEW"]}
     * → {"id": 5, "code": "LIBRARIAN", "status": "INACTIVE", ...}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(SecurityPermissions.ROLE_MANAGE)
    public ApiResponse<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(roleService.update(id, request, authorities(jwt)));
    }

    /**
     * Deletes a role admins made; refused for system roles and roles someone still holds. 204.
     * <pre>DELETE /api/v1/roles/5</pre>
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(SecurityPermissions.ROLE_MANAGE)
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }

    /** The "authorities" claim of the token as a set (empty when missing); used to check what the admin may give. */
    static Set<String> authorities(Jwt jwt) {
        List<String> list = jwt.getClaimAsStringList("authorities");
        return list == null ? Set.of() : new HashSet<>(list);
    }
}
