package com.ums.hostel.api.dto;

import com.ums.hostel.domain.HostelMaintenanceRequest;
import java.time.OffsetDateTime;

public record HostelMaintenanceRequestResponse(
        Long id,
        Long hostelRoomId,
        Long reportedBy,
        String description,
        HostelMaintenanceRequest.Priority priority,
        HostelMaintenanceRequest.Status status,
        OffsetDateTime resolvedAt) {
}
