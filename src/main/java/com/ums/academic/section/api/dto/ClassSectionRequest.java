package com.ums.academic.section.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClassSectionRequest(
        @NotNull Long courseId,
        @NotNull Long semesterId,
        @NotBlank @Size(max = 10) String sectionCode,
        @NotNull @Positive Integer capacity) {
}
