package com.ums.timetable.api.dto;

import com.ums.timetable.domain.TimetableConstraint;

public record TimetableConstraintResponse(
        Long id,
        Long semesterId,
        TimetableConstraint.ConstraintType constraintType,
        String scopeType,
        Long scopeId,
        String value,
        boolean hard,
        int weight) {
}
