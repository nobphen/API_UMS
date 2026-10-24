package com.ums.graduation.api.dto;

import com.ums.graduation.domain.Thesis;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ThesisResponse(
        Long id,
        Long studentId,
        Thesis.ThesisType thesisType,
        String title,
        String titleKh,
        String summary,
        String summaryKh,
        Thesis.Status status,
        LocalDate proposalApprovedOn,
        OffsetDateTime submittedAt,
        OffsetDateTime defenseAt,
        Long defenseRoomId,
        BigDecimal resultScore,
        String resultGrade,
        String fileUrl,
        OffsetDateTime createdAt) {
}
