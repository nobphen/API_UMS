package com.ums.hostel.api.dto;

import com.ums.hostel.domain.HostelMaintenanceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Reports a problem in a room; the reporter is the logged-in user. */
public record HostelMaintenanceRequestRequest(
        @NotNull Long hostelRoomId,
        @NotBlank String description,
        HostelMaintenanceRequest.Priority priority) {   // empty = MEDIUM
}
