package com.ums.academic.calendar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Data to add or change an academic year. Used by AcademicYearController (POST and PUT: the same fields).
 * No "current" here: PUT /academic-years/{id}/current makes a year the current one.
 * <pre>{"name": "2026-2027", "startDate": "2026-10-01", "endDate": "2027-09-30"}</pre>
 */
public record AcademicYearRequest(
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "{validation.academic_year_name}")
        String name,                  // always "2026-2027": the same form for every year, so the list sorts well
        @NotNull LocalDate startDate, // first day of the year, e.g. 2026-10-01
        @NotNull LocalDate endDate) { // last day, must be after startDate (checked in the service)
}
