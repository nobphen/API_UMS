package com.ums.security.role.application.mapper;

import com.ums.security.role.api.dto.RoleResponse;
import com.ums.security.role.domain.Permission;
import com.ums.security.role.domain.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct: Role entity → RoleResponse. */
@Mapper(componentModel = "spring", imports = Permission.class)
public interface RoleMapper {

    /** Copies the role fields; permissions become a sorted list of codes; userCount comes from the caller (a COUNT query). */
    @Mapping(target = "permissions",
            expression = "java(role.getPermissions().stream().map(Permission::getCode).sorted().toList())")
    RoleResponse toResponse(Role role, long userCount);
}
