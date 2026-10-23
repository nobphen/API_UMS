package com.ums.security.role.application.mapper;

import com.ums.security.role.api.dto.PermissionResponse;
import com.ums.security.role.domain.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);
}
