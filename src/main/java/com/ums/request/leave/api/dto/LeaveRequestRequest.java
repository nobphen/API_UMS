package com.ums.request.leave.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** A student asks for leave; the student is the logged-in user, the request number is generated. */
public record LeaveRequestRequest(
        @NotNull Long leaveTypeId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,   // on or after fromDate
        @NotBlank String reason) {
}
