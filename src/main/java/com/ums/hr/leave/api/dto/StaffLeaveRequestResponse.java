package com.ums.hr.leave.api.dto;

import com.ums.hr.leave.domain.StaffLeaveRequest;
import java.math.BigDecimal;
import java.time.LocalDate;

public record StaffLeaveRequestResponse(
        Long id,
        Long staffId,
        Long leaveTypeId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal days,
        String reason,
        StaffLeaveRequest.Status status,
        Long approvedBy) {
}
