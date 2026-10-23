package com.ums.finance.scholarship.api.dto;

import jakarta.validation.constraints.NotNull;

public record StudentScholarshipRequest(
        @NotNull Long studentId,
        @NotNull Long scholarshipId,
        @NotNull Long academicYearId) {
}
