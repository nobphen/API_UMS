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

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAllByOrderBySystemDescNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse get(Long id) {
        return toResponse(findRole(id));
    }

    @Override
    public RoleResponse create(RoleRequest request, Set<String> callerAuthorities) {
        if (roleRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Role code already exists: " + request.code());
        }
        Set<Permission> permissions = findPermissions(request.permissions());
        requireCanGrant(permissions, callerAuthorities);

        Role role = new Role();
        role.setCode(request.code());
        role.setName(request.name().trim());
        role.setDescription(request.description());
        role.setPermissions(permissions);
        roleRepository.save(role);
        return toResponse(role);
    }

    @Override
    public RoleResponse update(Long id, RoleUpdateRequest request, Set<String> callerAuthorities) {
        Role role = findRole(id);
        boolean superAdminRole = SUPER_ADMIN.equals(role.getCode());

        if (request.permissions() != null) {
            if (superAdminRole) {
                throw new BusinessException("SUPER_ADMIN always has every permission and can't be changed");
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
                throw new BusinessException("SUPER_ADMIN can't be made inactive");
            }
            role.setStatus(request.status());
        }
        role.setName(request.name().trim());
        role.setDescription(request.description());

        // Holders get the new permissions at their next token refresh
        userRoleRepository.findUserIdsByRoleId(id).forEach(permissionCacheService::evict);
        return toResponse(role);
    }

    @Override
    public void delete(Long id) {
        Role role = findRole(id);
        if (role.isSystem()) {
            throw new BusinessException("System roles can't be deleted; make the role INACTIVE instead");
        }
        long holders = userRoleRepository.countByRole_Id(id);
        if (holders > 0) {
            throw new BusinessException("The role is still given to " + holders + " user(s); remove it from them first");
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Role requireGivable(String roleCode, Set<String> callerAuthorities) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleCode));
        if (SUPER_ADMIN.equals(role.getCode())) {
            if (!callerAuthorities.contains(SUPER_ADMIN_AUTHORITY)) {
                throw new BusinessException("Only a SUPER_ADMIN can give or remove the SUPER_ADMIN role");
            }
            return role;
        }
        requireCanGrant(role.getPermissions(), callerAuthorities);
        return role;
    }

    /// HELPERS ------------------------------------------------------

    private Role findRole(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Role not found"));
    }

    private RoleResponse toResponse(Role role) {
        return roleMapper.toResponse(role, userRoleRepository.countByRole_Id(role.getId()));
    }

    private Set<Permission> findPermissions(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new HashSet<>();
        }
        Set<Permission> permissions = new HashSet<>(permissionRepository.findByCodeIn(codes));
        if (permissions.size() != codes.size()) {
            Set<String> found = permissions.stream().map(Permission::getCode).collect(Collectors.toSet());
            Set<String> unknown = new TreeSet<>(codes);
            unknown.removeAll(found);
            throw new BusinessException("Unknown permission codes: " + String.join(", ", unknown));
        }
        return permissions;
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
            throw new BusinessException("You can't give permissions you don't have: " + String.join(", ", missing));
        }
    }
}
