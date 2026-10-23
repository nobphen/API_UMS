package com.ums.timetable.api.dto;

import jakarta.validation.constraints.NotNull;

/** Starts the timetable generator for a semester; the starting user is taken from the login. */
public record TimetableRunRequest(
        @NotNull Long semesterId) {
}
