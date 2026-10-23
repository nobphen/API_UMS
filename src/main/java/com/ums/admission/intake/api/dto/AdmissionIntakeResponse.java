package com.ums.admission.intake.api.dto;

import com.ums.admission.intake.domain.AdmissionIntake;
import java.time.LocalDate;

public record AdmissionIntakeResponse(
        Long id,
        Long academicYearId,
        String name,
        String nameKh,
        LocalDate openDate,
        LocalDate closeDate,
        AdmissionIntake.Status status) {
}
