package com.ums.timetable.api.dto;

import com.ums.timetable.domain.TimetableRun;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TimetableRunResponse(
        Long id,
        Long semesterId,
        Long startedBy,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        TimetableRun.Status status,
        Integer conflictCount,
        BigDecimal score) {
}
