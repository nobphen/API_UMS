package com.ums.hostel.api.dto;

import com.ums.hostel.domain.HostelRoom;
import java.math.BigDecimal;

public record HostelRoomResponse(
        Long id,
        Long hostelId,
        String roomNo,
        Integer floor,
        HostelRoom.RoomType roomType,
        int capacity,
        BigDecimal pricePerSemester,
        String status) {
}
