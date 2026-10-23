package com.ums.lms.api.dto;

import java.time.OffsetDateTime;

public record AssignmentSubmissionResponse(
        Long id,
        Long assessmentId,
        Long enrollmentId,
        String fileUrl,
        String textAnswer,
        OffsetDateTime submittedAt,
        boolean late,
        int attemptNo,
        String feedback) {
}
