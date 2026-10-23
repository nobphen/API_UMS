package com.ums.security.role.application;

import com.ums.security.role.api.dto.RoleRequest;
import com.ums.security.role.api.dto.RoleResponse;
import com.ums.security.role.api.dto.RoleUpdateRequest;
import com.ums.security.role.domain.Role;
import java.util.List;
import java.util.Set;

/**
 * Roles are made and changed by admins at run time. callerAuthorities are the admin's own authorities:
 * nobody can put a permission into a role unless they hold it themselves (except SUPER_ADMIN).
 * Users pick up a change at their next token refresh, at most 15 minutes later.
 */
public interface RoleService {

    List<RoleResponse> list();

    RoleResponse get(Long id);

    RoleResponse create(RoleRequest request, Set<String> callerAuthorities);

    RoleResponse update(Long id, RoleUpdateRequest request, Set<String> callerAuthorities);

    /** Only roles admins made, and only when nobody holds them any more. */
    void delete(Long id);

    /**
     * The active role with this code, if the caller may give it to someone (or take it away): SUPER_ADMIN only
     * by a SUPER_ADMIN, any other role only by someone who holds all of its permissions.
     */
    Role requireGivable(String roleCode, Set<String> callerAuthorities);
}
