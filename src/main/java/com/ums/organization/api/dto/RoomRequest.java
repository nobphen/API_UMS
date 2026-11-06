package com.ums.organization.api.dto;

import com.ums.organization.domain.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Body to create or update a room. */
public record RoomRequest(
        @NotNull Long buildingId,                  // the building it is in
        @NotBlank @Size(max = 20) String code,     // unique within the building
        @NotNull @Positive Integer capacity,       // number of seats
        @NotNull Room.RoomType roomType) {
}
