package com.ums.exam.api.dto;

import com.ums.exam.domain.ExamSchedule;
import java.time.LocalDate;
import java.time.LocalTime;

public record ExamScheduleResponse(
        Long id,
        Long classSectionId,
        Long assessmentId,
        ExamSchedule.ExamType examType,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        Long roomId) {
}
