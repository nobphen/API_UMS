package com.ums.academic.course.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CurriculumRequest(
        @NotNull Long programId,
        @NotBlank @Size(max = 20) String version,
        @NotNull Integer effectiveYear,
        @Size(max = 20) String status) {   // empty = ACTIVE
}
