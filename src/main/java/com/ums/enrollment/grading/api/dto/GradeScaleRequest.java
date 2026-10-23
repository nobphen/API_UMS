package com.ums.enrollment.grading.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record GradeScaleRequest(
        @NotBlank @Size(max = 3) String letter,   // e.g. A, B+
        @NotNull @PositiveOrZero BigDecimal minScore,
        @NotNull @PositiveOrZero BigDecimal maxScore,
        @NotNull @PositiveOrZero BigDecimal gradePoint,
        Boolean pass) {   // empty = true
}
