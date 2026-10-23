package com.ums.transport.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;

public record TransportRouteRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameKh,
        Long vehicleId,
        LocalTime morningDeparture,
        LocalTime eveningDeparture,
        @PositiveOrZero BigDecimal feePerSemester,   // empty = 0
        @Size(max = 20) String status) {   // empty = ACTIVE
}
