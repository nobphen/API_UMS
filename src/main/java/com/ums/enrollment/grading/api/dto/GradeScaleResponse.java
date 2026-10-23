package com.ums.enrollment.grading.api.dto;

import java.math.BigDecimal;

public record GradeScaleResponse(
        Long id,
        String letter,
        BigDecimal minScore,
        BigDecimal maxScore,
        BigDecimal gradePoint,
        boolean pass) {
}
