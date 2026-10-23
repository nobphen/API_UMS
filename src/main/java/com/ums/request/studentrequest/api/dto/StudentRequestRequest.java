package com.ums.request.studentrequest.api.dto;

import com.ums.request.studentrequest.domain.StudentRequest;
import jakarta.validation.constraints.NotNull;

/** A student files a request; the student is the logged-in user, the request number is generated. */
public record StudentRequestRequest(
        @NotNull StudentRequest.RequestType requestType,
        String payload,   // type-specific details as JSON
        String reason) {
}
