package com.ums.admission.application.api.dto;

import com.ums.admission.application.domain.Application;
import java.time.OffsetDateTime;

public record ApplicationResponse(
        Long id,
        String applicationNo,
        Long applicantId,
        Long intakeId,
        Long formSaleId,
        Long firstChoiceProgramId,
        Long secondChoiceProgramId,
        Long admittedProgramId,
        Application.Status status,
        Application.PreviousLevel previousLevel,
        String previousInstitution,
        String previousMajor,
        Integer previousYear,
        String previousResult,
        Long previousStudentId,
        OffsetDateTime submittedAt,
        Long decidedBy,
        OffsetDateTime decidedAt) {
}
