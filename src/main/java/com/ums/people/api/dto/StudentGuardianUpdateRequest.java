package com.ums.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Changes one link: the relationship and whether this guardian is the primary contact.
 * Used by PUT /students/{id}/guardians/{guardianId}. To link another guardian: unlink, then link again.
 * <pre>{"relationship": "MOTHER", "primary": true}</pre>
 */
public record StudentGuardianUpdateRequest(
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[A-Z_]+$") String relationship,   // FATHER, MOTHER, UNCLE ...
        boolean primary) {
}
