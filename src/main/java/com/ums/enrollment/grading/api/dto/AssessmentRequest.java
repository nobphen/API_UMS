package com.ums.enrollment.grading.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssessmentRequest(
        @NotNull Long classSectionId,
        @NotNull Long assessmentTypeId,
        @NotBlank @Size(max = 150) String title,
        @NotNull @Positive BigDecimal maxScore,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal weightPercent,   // share of the final grade
        LocalDate dueDate) {
}
