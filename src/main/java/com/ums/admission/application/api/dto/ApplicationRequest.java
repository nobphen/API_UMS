package com.ums.admission.application.api.dto;

import com.ums.admission.application.domain.Application;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** An application for an intake. The application number is generated. */
public record ApplicationRequest(
        @NotNull Long applicantId,
        @NotNull Long intakeId,
        Long formSaleId,   // the paper form bought at the counter, if any
        @NotNull Long firstChoiceProgramId,
        Long secondChoiceProgramId,

        // the last study done before this application
        Application.PreviousLevel previousLevel,
        @Size(max = 200) String previousInstitution,
        @Size(max = 150) String previousMajor,
        Integer previousYear,
        @Size(max = 20) String previousResult,   // GPA, grade or mention, as written on the certificate
        Long previousStudentId) {   // set when they studied here before
}
