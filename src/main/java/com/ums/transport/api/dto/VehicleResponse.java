package com.ums.transport.api.dto;

import com.ums.transport.domain.Vehicle;

public record VehicleResponse(
        Long id,
        String plateNo,
        Vehicle.VehicleType vehicleType,
        int capacity,
        String driverName,
        String driverPhone,
        String status) {
}
