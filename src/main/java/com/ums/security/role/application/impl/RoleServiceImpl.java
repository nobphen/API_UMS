package com.ums.security.role.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.security.auth.application.PermissionCacheService;
import com.ums.security.role.api.dto.RoleRequest;
import com.ums.security.role.api.dto.RoleResponse;
import com.ums.security.role.api.dto.RoleUpdateRequest;
import com.ums.security.role.application.RoleService;
import com.ums.security.role.application.mapper.RoleMapper;
import com.ums.security.role.domain.Permission;
import com.ums.security.role.domain.Role;
import com.ums.security.role.infrastructure.PermissionRepository;
import com.ums.security.role.infrastructure.RoleRepository;
import com.ums.security.user.infrastructure.UserRoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role add / change / delete, with the rule that an admin can't give permissions they don't hold.
 * After a change it clears the permission cache of every holder.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    static final String SUPER_ADMIN = "SUPER_ADMIN";
    static final String SUPER_ADMIN_AUTHORITY = "ROLE_" + SUPER_ADMIN;

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionCacheService permissionCacheService;
    private final RoleMapper roleMapper;

    /** One query, system roles first then by name; each role → response with its holder count. */
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAllByOrderBySystemDescNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    /** One role by id (404 when missing) → response. */
    @Override
    @Transactional(readOnly = true)
    public RoleResponse get(Long id) {
        return toResponse(findRole(id));
    }

    /** Code must be free → permission codes must exist → caller must hold them all → INSERT. */
    @Override
    public RoleResponse create(RoleRequest request, Set<String> callerAuthorities) {
        if (roleRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("error.role.code_exists", request.code());
        }
        Set<Permission> permissions = findPermissions(request.permissions());
        requireCanGrant(permissions, callerAuthorities);

        Role role = new Role();
        role.setCode(request.code());
        role.setName(request.name().trim());
        role.setDescription(request.description());
        role.setNameKh(blankToNull(request.nameKh()));
        role.setDescriptionKh(blankToNull(request.descriptionKh()));
        role.setPermissions(permissions);
        roleRepository.save(role);
        return toResponse(role);
    }

    /**
     * Replaces the permissions (only the added ones are checked against the caller), changes status and names,
     * then clears the permission cache of every holder. SUPER_ADMIN's permissions and status can't change.
     */
    @Override
    public RoleResponse update(Long id, RoleUpdateRequest request, Set<String> callerAuthorities) {
        Role role = findRole(id);
        boolean superAdminRole = SUPER_ADMIN.equals(role.getCode());

        if (request.permissions() != null) {
            if (superAdminRole) {
                throw new BusinessException("error.role.super_admin_permissions");
            }
            Set<Permission> permissions = findPermissions(request.permissions());
            Set<Permission> added = new HashSet<>(permissions);
            added.removeAll(role.getPermissions());
            requireCanGrant(added, callerAuthorities);   // taking permissions away is always allowed
            role.getPermissions().clear();
            role.getPermissions().addAll(permissions);
        }
        if (request.status() != null && !request.status().equals(role.getStatus())) {
            if (superAdminRole) {
                throw new BusinessException("error.role.super_admin_inactive");
            }
            role.setStatus(request.status());
        }
        role.setName(request.name().trim());
        role.setDescription(request.description());
        role.setNameKh(blankToNull(request.nameKh()));
        role.setDescriptionKh(blankToNull(request.descriptionKh()));

        // Holders get the new permissions at their next token refresh
        userRoleRepository.findUserIdsByRoleId(id).forEach(permissionCacheService::evict);
        return toResponse(role);
    }

    /** Refuses a system role or a role someone still holds; otherwise deletes it. */
    @Override
    public void delete(Long id) {
        Role role = findRole(id);
        if (role.isSystem()) {
            throw new BusinessException("error.role.system_delete");
        }
        long holders = userRoleRepository.countByRole_Id(id);
        if (holders > 0) {
            throw new BusinessException("error.role.in_use", holders);
        }
        roleRepository.delete(role);
    }

    /** Finds the role by code (404), then: SUPER_ADMIN needs a SUPER_ADMIN caller, others need all their permissions. */
    @Override
    @Transactional(readOnly = true)
    public Role requireGivable(String roleCode, Set<String> callerAuthorities) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new NotFoundException("error.role.not_found_code", roleCode));
        if (SUPER_ADMIN.equals(role.getCode())) {
            if (!callerAuthorities.contains(SUPER_ADMIN_AUTHORITY)) {
                throw new BusinessException("error.role.super_admin_only");
            }
            return role;
        }
        requireCanGrant(role.getPermissions(), callerAuthorities);
        return role;
    }

    /// HELPERS ------------------------------------------------------

    /** The role, or 404 when it does not exist. */
    private Role findRole(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new NotFoundException("error.role.not_found"));
    }

    /** Role → response, with how many users hold it. */
    private RoleResponse toResponse(Role role) {
        return roleMapper.toResponse(role, userRoleRepository.countByRole_Id(role.getId()));
    }

    /** The permissions with these codes; 400 listing every unknown code. Empty or null = no permissions. */
    private Set<Permission> findPermissions(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new HashSet<>();
        }
        Set<Permission> permissions = new HashSet<>(permissionRepository.findByCodeIn(codes));
        if (permissions.size() != codes.size()) {
            Set<String> found = permissions.stream().map(Permission::getCode).collect(Collectors.toSet());
            Set<String> unknown = new TreeSet<>(codes);
            unknown.removeAll(found);
            throw new BusinessException("error.role.unknown_permissions", String.join(", ", unknown));
        }
        return permissions;
    }

    /** An empty Khmer name means "not translated": the app then shows the English one. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** Stops an admin with ROLE_MANAGE from making a role more powerful than themselves. */
    private static void requireCanGrant(Set<Permission> permissions, Set<String> callerAuthorities) {
        if (callerAuthorities.contains(SUPER_ADMIN_AUTHORITY)) {
            return;
        }
        Set<String> missing = permissions.stream()
                .map(Permission::getCode)
                .filter(code -> !callerAuthorities.contains(code))
                .collect(Collectors.toCollection(TreeSet::new));
        if (!missing.isEmpty()) {
            throw new BusinessException("error.role.cannot_grant", String.join(", ", missing));
        }
    }
}
