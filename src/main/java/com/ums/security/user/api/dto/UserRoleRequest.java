package com.ums.security.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Gives a user a role, for the whole university (GLOBAL) or only part of it,
 * e.g. DEAN for one faculty, or LECTURER for one class section. Dates limit how long it lasts.
 */
public record UserRoleRequest(
        @NotBlank String roleCode,

        @Pattern(regexp = "^(GLOBAL|FACULTY|DEPARTMENT|PROGRAM|CLASS_SECTION)$",
                message = "{validation.scope_type}")
        String scopeType,     // empty = GLOBAL

        Long scopeId,         // id of the faculty, department, program or class section; empty for GLOBAL
        LocalDate validFrom,  // empty = from today
        LocalDate validTo) {  // empty = no end
}
