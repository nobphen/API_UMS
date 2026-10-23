package com.ums.hostel.api.dto;

import com.ums.hostel.domain.HostelAllocation;
import java.time.LocalDate;

public record HostelAllocationResponse(
        Long id,
        Long studentId,
        Long bedId,
        Long semesterId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long invoiceId,
        HostelAllocation.Status status) {
}
