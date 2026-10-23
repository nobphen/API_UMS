package com.ums.academic.calendar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AcademicYearRequest(
        @NotBlank @Size(max = 20) String name,   // e.g. 2026-2027
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}
