package com.ums.security.role.application.mapper;

import com.ums.security.role.api.dto.PermissionResponse;
import com.ums.security.role.domain.Permission;
import org.mapstruct.Mapper;

/** MapStruct: Permission entity → PermissionResponse. */
@Mapper(componentModel = "spring")
public interface PermissionMapper {

    /** Copies id, code, module, action and both descriptions; nothing is ignored. */
    PermissionResponse toResponse(Permission permission);
}
