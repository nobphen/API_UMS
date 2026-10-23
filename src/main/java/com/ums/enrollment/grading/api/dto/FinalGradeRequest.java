package com.ums.enrollment.grading.api.dto;

import jakarta.validation.constraints.NotNull;

/** Creates the DRAFT grade of an enrollment; score, letter and grade point are computed from the assessment scores. */
public record FinalGradeRequest(
        @NotNull Long enrollmentId) {
}
