package com.ums.enrollment.grading.api.dto;

import com.ums.enrollment.grading.domain.FinalGrade;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FinalGradeResponse(
        Long id,
        Long enrollmentId,
        BigDecimal totalScore,
        String letterGrade,
        BigDecimal gradePoint,
        FinalGrade.Status status,
        Long approvedBy,
        OffsetDateTime publishedAt) {
}
