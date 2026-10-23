package com.ums.enrollment.grading.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssessmentResponse(
        Long id,
        Long classSectionId,
        Long assessmentTypeId,
        String title,
        BigDecimal maxScore,
        BigDecimal weightPercent,
        LocalDate dueDate) {
}
