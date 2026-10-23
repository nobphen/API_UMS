package com.ums.request.leave.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LeaveTypeRequest(
        @NotBlank @Size(max = 20) String code,   // e.g. SICK
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameKh,
        Boolean requiresDocument,   // empty = false
        @Positive Integer maxDays) {   // empty = no limit
}
