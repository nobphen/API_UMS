package com.ums.enrollment.attendance.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceSessionRequest(
        @NotNull Long classSectionId,
        @NotNull LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        @Size(max = 255) String topic) {
}
