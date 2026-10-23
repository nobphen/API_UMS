package com.ums.hostel.api.dto;

import jakarta.validation.constraints.NotNull;

/** Reserves a bed for a student for one semester; check-in, check-out and billing are separate steps. */
public record HostelAllocationRequest(
        @NotNull Long studentId,
        @NotNull Long bedId,
        @NotNull Long semesterId) {
}
