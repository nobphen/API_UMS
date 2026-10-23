package com.ums.transport.api.dto;

import com.ums.transport.domain.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank @Size(max = 20) String plateNo,
        @NotNull Vehicle.VehicleType vehicleType,
        @NotNull @Positive Integer capacity,   // number of seats
        @Size(max = 100) String driverName,
        @Size(max = 30) String driverPhone,
        @Size(max = 20) String status) {   // empty = ACTIVE
}
