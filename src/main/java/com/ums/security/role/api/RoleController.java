package com.ums.security.role.api;

import com.ums.common.response.ApiResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).ROLE_VIEW)")
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.ok(roleService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).ROLE_VIEW)")
    public ApiResponse<RoleResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(roleService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).ROLE_MANAGE)")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(roleService.create(request, authorities(jwt)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).ROLE_MANAGE)")
    public ApiResponse<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(roleService.update(id, request, authorities(jwt)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).ROLE_MANAGE)")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }

    static Set<String> authorities(Jwt jwt) {
        List<String> list = jwt.getClaimAsStringList("authorities");
        return list == null ? Set.of() : new HashSet<>(list);
    }
}
