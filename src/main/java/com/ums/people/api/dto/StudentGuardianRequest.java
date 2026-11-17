package com.ums.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Links a guardian to a student. Used by POST /students/{id}/guardians.
 * <pre>{"guardianId": 1, "relationship": "FATHER", "primary": true}</pre>
 */
public record StudentGuardianRequest(
        @NotNull Long guardianId,
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[A-Z_]+$") String relationship,   // FATHER, MOTHER, UNCLE, GRANDMOTHER ...
        boolean primary) {   // true = the guardian the school calls first (only one per student)
}
