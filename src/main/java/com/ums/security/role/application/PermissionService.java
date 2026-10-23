package com.ums.security.role.application;

import com.ums.security.role.api.dto.PermissionResponse;
import java.util.List;

/**
 * Permissions are fixed: each code is checked by @PreAuthorize in the code and seeded by V2__seed_permissions.sql.
 * Admins don't create them; they combine them into roles.
 */
public interface PermissionService {

    /** Every permission, sorted by module then code. */
    List<PermissionResponse> list();
}
