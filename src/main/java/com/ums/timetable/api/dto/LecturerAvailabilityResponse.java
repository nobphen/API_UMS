package com.ums.timetable.api.dto;

import java.time.LocalTime;

public record LecturerAvailabilityResponse(
        Long id,
        Long staffId,
        Long semesterId,
        int dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean available) {
}
