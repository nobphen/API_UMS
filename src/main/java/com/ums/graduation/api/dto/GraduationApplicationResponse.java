package com.ums.graduation.api.dto;

import com.ums.graduation.domain.GraduationApplication;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record GraduationApplicationResponse(
        Long id,
        Long studentId,
        Long academicYearId,
        Integer creditsCompleted,
        BigDecimal cgpa,
        boolean requirementsMet,
        boolean feesCleared,
        GraduationApplication.Status status,
        Long approvedBy,
        OffsetDateTime createdAt) {
}
