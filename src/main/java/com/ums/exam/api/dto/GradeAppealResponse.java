package com.ums.exam.api.dto;

import com.ums.exam.domain.GradeAppeal;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record GradeAppealResponse(
        Long id,
        Long finalGradeId,
        Long studentId,
        String reason,
        GradeAppeal.Status status,
        BigDecimal oldScore,
        BigDecimal newScore,
        Long reviewedBy,
        OffsetDateTime createdAt) {
}
