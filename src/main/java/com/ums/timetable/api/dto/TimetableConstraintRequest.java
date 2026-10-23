package com.ums.timetable.api.dto;

import com.ums.timetable.domain.TimetableConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TimetableConstraintRequest(
        @NotNull Long semesterId,
        @NotNull TimetableConstraint.ConstraintType constraintType,
        @Size(max = 20) String scopeType,
        Long scopeId,
        String value,     // JSON parameters of the rule
        Boolean hard,     // empty = true
        Integer weight) { // empty = 1
}
