package com.ums.hostel.api.dto;

import com.ums.hostel.domain.HostelRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record HostelRoomRequest(
        @NotNull Long hostelId,
        @NotBlank @Size(max = 20) String roomNo,   // unique within the hostel
        Integer floor,
        @NotNull HostelRoom.RoomType roomType,
        @NotNull @Positive Integer capacity,   // number of beds
        @NotNull @PositiveOrZero BigDecimal pricePerSemester,
        @Size(max = 20) String status) {   // empty = ACTIVE
}
