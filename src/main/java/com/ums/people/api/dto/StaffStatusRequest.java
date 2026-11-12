package com.ums.people.api.dto;

import com.ums.people.domain.Staff;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A real event in the staff member's job: on leave, back to ACTIVE, resigned, terminated or retired. */
public record StaffStatusRequest(
        @NotNull Staff.Status status,
        @Size(max = 255) String reason) {   // e.g. "Moved to another university"
}
