package com.ums.admission.intake.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** A new intake starts as DRAFT; it is opened and closed through its own actions. */
public record AdmissionIntakeRequest(
        @NotNull Long academicYearId,
        @NotBlank @Size(max = 100) String name,
        @NotNull LocalDate openDate,
        @NotNull LocalDate closeDate) {
}
