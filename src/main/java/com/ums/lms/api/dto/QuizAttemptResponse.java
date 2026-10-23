package com.ums.lms.api.dto;

import com.ums.lms.domain.QuizAttempt;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record QuizAttemptResponse(
        Long id,
        Long assessmentId,
        Long enrollmentId,
        int attemptNo,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        String answers,
        BigDecimal score,
        QuizAttempt.Status status) {
}
