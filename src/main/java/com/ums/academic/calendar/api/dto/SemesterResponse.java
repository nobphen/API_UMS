package com.ums.academic.calendar.api.dto;

import java.time.LocalDate;

/**
 * One semester as the client sees it. academicYearName is sent too, so a list can show "2026-2027"
 * without a second call.
 */
public record SemesterResponse(
        Long id,
        Long academicYearId,         // e.g. 2
        String academicYearName,     // e.g. 2026-2027
        String name,                 // e.g. Semester 1
        String nameKh,               // e.g. ឆមាសទី១
        int semesterNo,              // 1, 2, 3
        LocalDate startDate,
        LocalDate endDate,
        LocalDate registrationStart, // null when not set
        LocalDate registrationEnd,
        boolean current) {           // the semester now running (only one)
}
