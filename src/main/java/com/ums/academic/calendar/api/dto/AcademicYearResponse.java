package com.ums.academic.calendar.api.dto;

import java.time.LocalDate;

/**
 * An academic year sent back by AcademicYearController (create, get, list, current, update).
 * <pre>{"id": 1, "name": "2026-2027", "startDate": "2026-10-01", "endDate": "2027-09-30", "current": true}</pre>
 */
public record AcademicYearResponse(
        Long id,
        String name,          // 2026-2027
        LocalDate startDate,
        LocalDate endDate,
        boolean current) {    // true = the year now running (only one)
}
