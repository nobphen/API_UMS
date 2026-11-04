package com.ums.organization.application;

import com.ums.organization.api.dto.RoomRequest;
import com.ums.organization.api.dto.RoomResponse;
import com.ums.organization.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Rooms in campus buildings: create, read, change and (soft) delete. Used by RoomController. */
public interface RoomService {

    /**
     * Adds a room to a building. The code is saved in capitals (" r101 " → "R101").
     *
     * @param request building, code, capacity and room type
     * @return the new room, with its id
     * @throws com.ums.common.exception.NotFoundException error.building.not_found when the building does not exist
     *                                                    or was deleted
     * @throws com.ums.common.exception.BusinessException error.room.code_exists when the building already has a room
     *                                                    with the code, a deleted one included
     */
    RoomResponse create(RoomRequest request);

    /**
     * One room.
     *
     * @param id room id
     * @return the room
     * @throws com.ums.common.exception.NotFoundException error.room.not_found when it does not exist or was deleted
     */
    RoomResponse get(Long id);

    /**
     * One page of rooms, deleted ones left out.
     *
     * @param search     filters by code; null or empty = all
     * @param buildingId only rooms of this building; null = every building
     * @param roomType   only rooms of this type; null = every type
     * @param pageable   page number, size and sort (?page=1&size=20&sort=code,asc)
     * @return the rooms of that page, with the totals
     */
    Page<RoomResponse> list(String search, Long buildingId, Room.RoomType roomType, Pageable pageable);

    /**
     * Replaces all the room's fields with the request (code in capitals). The room can move to another building.
     *
     * @param id      room id
     * @param request the new values; the room may keep its own code
     * @return the room after the change
     * @throws com.ums.common.exception.NotFoundException error.room.not_found or error.building.not_found when it
     *                                                    does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.room.code_exists when another room in the building
     *                                                    has the code
     */
    RoomResponse update(Long id , RoomRequest request);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its code can't be used again in the building.
     *
     * @param id room id
     * @throws com.ums.common.exception.NotFoundException error.room.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.room.in_use while class schedules, exam schedules
     *                                                    or assets still use it
     */
    void delete(Long id);
}
