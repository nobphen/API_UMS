package com.ums.transport.api.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record TransportRouteResponse(
        Long id,
        String code,
        String name,
        Long vehicleId,
        LocalTime morningDeparture,
        LocalTime eveningDeparture,
        BigDecimal feePerSemester,
        String status) {
}
