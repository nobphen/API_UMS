package com.ums.academic.calendar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Data to add or change a semester. Used by SemesterController (POST and PUT: the same fields).
 * No "current" here: PUT /semesters/{id}/current makes a semester the current one.
 * <pre>
 * {"academicYearId": 2, "name": "Semester 1", "nameKh": "ឆមាសទី១", "semesterNo": 1,
 *  "startDate": "2026-10-01", "endDate": "2027-02-28",
 *  "registrationStart": "2026-09-15", "registrationEnd": "2026-10-10"}
 * </pre>
 */
public record SemesterRequest(
        @NotNull Long academicYearId,             // the year it belongs to
        @NotBlank @Size(max = 50) String name,    // e.g. Semester 1
        @Size(max = 50) String nameKh,            // e.g. ឆមាសទី១ (optional)
        @NotNull @Positive Integer semesterNo,    // order in the year: 1, 2, 3 (summer); unique per year
        @NotNull LocalDate startDate,             // first day, inside the academic year
        @NotNull LocalDate endDate,               // last day, after startDate, inside the academic year
        LocalDate registrationStart,              // students can register for classes from this day (optional)
        LocalDate registrationEnd) {              // ... until this day; both dates or none
}
