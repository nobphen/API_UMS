package com.ums.request.leave.api.dto;

import com.ums.request.leave.domain.LeaveRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LeaveRequestResponse(
        Long id,
        String requestNo,
        Long studentId,
        Long leaveTypeId,
        LocalDate fromDate,
        LocalDate toDate,
        String reason,
        LeaveRequest.Status status,
        Long conversationId,
        OffsetDateTime createdAt) {
}
