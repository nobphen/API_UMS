package com.ums.people.api.dto;

import com.ums.people.domain.GeoArea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** A new province, district, commune or village, e.g. after a sub-decree creates one. */
public record GeoAreaRequest(
        @NotBlank
        @Pattern(regexp = "^(\\d{2}|\\d{4}|\\d{6}|\\d{8})$", message = "must be the official 2, 4, 6 or 8 digit code")
        String code,              // must start with the parent's code

        @NotNull GeoArea.Level level,
        Long parentId,            // empty for a province
        @NotBlank @Size(max = 150) String nameKh,
        @NotBlank @Size(max = 150) String nameEn,
        @Size(max = 30) String unitKh,   // e.g. ឃុំ or សង្កាត់
        @Size(max = 30) String unitEn) { // e.g. Commune or Quarter
}
