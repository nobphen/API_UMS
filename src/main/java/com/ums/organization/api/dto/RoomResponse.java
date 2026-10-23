package com.ums.organization.api.dto;

import com.ums.organization.domain.Room;

public record RoomResponse(
        Long id,
        Long buildingId,
        String code,
        int capacity,
        Room.RoomType roomType) {
}
