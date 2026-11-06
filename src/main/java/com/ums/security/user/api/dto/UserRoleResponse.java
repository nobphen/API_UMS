package com.ums.security.user.api.dto;

import java.time.LocalDate;

/** One role given to a user: the role, its scope (GLOBAL, FACULTY, ... + scopeId) and the dates it is valid. */
public record UserRoleResponse(
        Long id,
        String roleCode,
        String roleName,
        String scopeType,
        Long scopeId,
        LocalDate validFrom,
        LocalDate validTo) {
}
