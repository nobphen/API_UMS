package com.ums.security.role.application.impl;

import com.ums.security.role.api.dto.PermissionResponse;
import com.ums.security.role.application.PermissionService;
import com.ums.security.role.application.mapper.PermissionMapper;
import com.ums.security.role.infrastructure.PermissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lists the permissions from the database; read-only, because permissions come from the seed scripts. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    /** Every permission code, grouped by module, then by code; each row → response. */
    @Override
    public List<PermissionResponse> list() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(permissionMapper::toResponse)
                .toList();
    }
}
