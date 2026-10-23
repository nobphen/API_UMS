package com.ums.academic.calendar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SemesterRequest(
        @NotNull Long academicYearId,
        @NotBlank @Size(max = 50) String name,
        @NotNull @Positive Integer semesterNo,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        LocalDate registrationStart,
        LocalDate registrationEnd) {
}
