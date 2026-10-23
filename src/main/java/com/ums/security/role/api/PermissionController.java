package com.ums.security.role.api;

import com.ums.common.response.ApiResponse;
import com.ums.security.role.api.dto.PermissionResponse;
import com.ums.security.role.application.PermissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only: permissions come from the code, admins combine them into roles. */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.ums.security.SecurityPermissions).ROLE_VIEW)")
    public ApiResponse<List<PermissionResponse>> list() {
        return ApiResponse.ok(permissionService.list());
    }
}
