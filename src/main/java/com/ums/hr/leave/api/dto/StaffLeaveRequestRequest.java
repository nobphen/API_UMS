package com.ums.hr.leave.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/** A staff member asks for leave; it starts PENDING until approved or rejected. */
public record StaffLeaveRequestRequest(
        @NotNull Long staffId,
        @NotNull Long leaveTypeId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,   // on or after fromDate
        @NotNull @Positive BigDecimal days,   // half days allowed, e.g. 0.5
        String reason) {
}
