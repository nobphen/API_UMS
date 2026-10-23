package com.ums.lms.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A student hands in work; the attempt number, submission time and lateness are set by the system. */
public record AssignmentSubmissionRequest(
        @NotNull Long assessmentId,
        @NotNull Long enrollmentId,
        @Size(max = 500) String fileUrl,
        String textAnswer) {
}
