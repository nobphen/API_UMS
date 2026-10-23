package com.ums.people.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Renames an area or switches it off. The code, level and parent never change: when a sub-decree merges or
 * moves areas, add the new area with its new code and make the old one inactive.
 */
public record GeoAreaUpdateRequest(
        @NotBlank @Size(max = 150) String nameKh,
        @NotBlank @Size(max = 150) String nameEn,
        @Size(max = 30) String unitKh,
        @Size(max = 30) String unitEn,
        Boolean active) {   // empty = unchanged
}
