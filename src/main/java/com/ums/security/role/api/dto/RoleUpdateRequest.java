package com.ums.security.role.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** The code can't change (code and SQL refer to it); everything else can. */
public record RoleUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameKh,
        String description,
        String descriptionKh,

        @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "must be ACTIVE or INACTIVE")
        String status,              // INACTIVE: users keep the role, but it grants nothing

        Set<String> permissions) {  // replaces the role's permissions; leave out to keep them
}
