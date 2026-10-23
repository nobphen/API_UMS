package com.ums.enrollment.grading.api.dto;

import jakarta.validation.constraints.NotNull;

/** Asks for a student's semester result; credits, GPA, CGPA and standing are computed from the final grades. */
public record StudentSemesterResultRequest(
        @NotNull Long studentId,
        @NotNull Long semesterId) {
}
