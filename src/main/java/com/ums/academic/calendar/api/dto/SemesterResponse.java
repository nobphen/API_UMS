package com.ums.academic.calendar.api.dto;

import java.time.LocalDate;

public record SemesterResponse(
        Long id,
        Long academicYearId,
        String name,
        String nameKh,
        int semesterNo,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate registrationStart,
        LocalDate registrationEnd,
        boolean current) {
}
