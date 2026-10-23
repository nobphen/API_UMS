package com.ums.security.role.application.impl;

import com.ums.security.role.api.dto.PermissionResponse;
import com.ums.security.role.application.PermissionService;
import com.ums.security.role.application.mapper.PermissionMapper;
import com.ums.security.role.infrastructure.PermissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionResponse> list() {
        return permissionRepository.findAllByOrderByModuleAscCodeAsc().stream()
                .map(permissionMapper::toResponse)
                .toList();
    }
}
