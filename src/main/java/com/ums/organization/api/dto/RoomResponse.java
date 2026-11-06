package com.ums.organization.api.dto;

import com.ums.organization.domain.Room;

/** A room as sent back by the API. */
public record RoomResponse(
        Long id,
        Long buildingId,
        String code,
        int capacity,
        Room.RoomType roomType) {
}
