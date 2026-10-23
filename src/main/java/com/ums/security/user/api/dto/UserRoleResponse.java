package com.ums.security.user.api.dto;

import java.time.LocalDate;

public record UserRoleResponse(
        Long id,
        String roleCode,
        String roleName,
        String scopeType,
        Long scopeId,
        LocalDate validFrom,
        LocalDate validTo) {
}
