package com.ums.lms.api.dto;

import jakarta.validation.constraints.NotNull;

/** A student starts (or saves) a quiz attempt; the attempt number, times, score and status are set by the system. */
public record QuizAttemptRequest(
        @NotNull Long assessmentId,
        @NotNull Long enrollmentId,
        String answers) {   // the answers per question, as JSON
}
