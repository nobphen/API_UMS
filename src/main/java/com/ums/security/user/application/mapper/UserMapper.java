package com.ums.security.user.application.mapper;

import com.ums.security.user.api.dto.UserRoleResponse;
import com.ums.security.user.domain.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for the user module (entity → response DTO). */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /** UserRole → UserRoleResponse; roleCode and roleName are copied from the linked role. */
    @Mapping(target = "roleCode", source = "role.code")
    @Mapping(target = "roleName", source = "role.name")
    UserRoleResponse toResponse(UserRole userRole);
}
