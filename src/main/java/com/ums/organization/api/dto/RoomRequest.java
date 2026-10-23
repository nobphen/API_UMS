package com.ums.organization.api.dto;

import com.ums.organization.domain.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RoomRequest(
        @NotNull Long buildingId,
        @NotBlank @Size(max = 20) String code,
        @NotNull @Positive Integer capacity,
        @NotNull Room.RoomType roomType) {
}
