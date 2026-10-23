package com.ums.timetable.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record LecturerAvailabilityRequest(
        @NotNull Long staffId,
        @NotNull Long semesterId,
        @NotNull @Min(1) @Max(7) Integer dayOfWeek,   // 1 = Monday ... 7 = Sunday
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,                   // must be after startTime
        Boolean available) {                          // empty = true
}
