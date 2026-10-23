package com.ums.enrollment.registration.api.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull Long studentId,
        @NotNull Long classSectionId,
        Boolean retake) {   // empty = false
}
