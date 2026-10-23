package com.ums.security.role.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** A new role made by an admin, e.g. LIBRARIAN or DEAN. */
public record RoleRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "must be UPPER_CASE letters, digits and '_', e.g. LIBRARIAN")
        String code,

        @NotBlank @Size(max = 100) String name,
        String description,
        Set<String> permissions) {   // permission codes, e.g. BOOK_MANAGE
}
