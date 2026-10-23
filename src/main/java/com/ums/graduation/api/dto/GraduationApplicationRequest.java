package com.ums.graduation.api.dto;

import jakarta.validation.constraints.NotNull;

/** Credits, CGPA and the requirement and fee checks are filled in when the registrar verifies it. */
public record GraduationApplicationRequest(
        @NotNull Long studentId,
        @NotNull Long academicYearId) {
}
