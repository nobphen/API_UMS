package com.ums.enrollment.registration.api.dto;

import com.ums.enrollment.registration.domain.Enrollment;
import java.time.OffsetDateTime;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        Long classSectionId,
        OffsetDateTime enrolledAt,
        Enrollment.Status status,
        boolean retake) {
}
