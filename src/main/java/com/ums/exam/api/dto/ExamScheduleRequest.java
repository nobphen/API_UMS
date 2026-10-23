package com.ums.exam.api.dto;

import com.ums.exam.domain.ExamSchedule;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ExamScheduleRequest(
        @NotNull Long classSectionId,
        Long assessmentId,   // the assessment the exam is scored under
        @NotNull ExamSchedule.ExamType examType,
        @NotNull LocalDate examDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        Long roomId) {
}
