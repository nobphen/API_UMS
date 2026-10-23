package com.ums.enrollment.grading.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssessmentTypeRequest(
        @NotBlank @Size(max = 20) String code,   // e.g. QUIZ, MIDTERM
        @NotBlank @Size(max = 50) String name,
        @Size(max = 50) String nameKh) {
}
