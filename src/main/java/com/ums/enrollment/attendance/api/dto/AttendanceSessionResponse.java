package com.ums.enrollment.attendance.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceSessionResponse(
        Long id,
        Long classSectionId,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        String topic,
        Long takenBy) {
}
