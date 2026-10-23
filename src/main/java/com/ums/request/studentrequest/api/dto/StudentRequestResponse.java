package com.ums.request.studentrequest.api.dto;

import com.ums.request.studentrequest.domain.StudentRequest;
import java.time.OffsetDateTime;

public record StudentRequestResponse(
        Long id,
        String requestNo,
        Long studentId,
        StudentRequest.RequestType requestType,
        String payload,
        String reason,
        StudentRequest.Status status,
        Long conversationId,
        OffsetDateTime createdAt) {
}
