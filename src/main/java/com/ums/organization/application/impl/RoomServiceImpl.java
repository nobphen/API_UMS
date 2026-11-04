package com.ums.organization.application.impl;

import com.ums.academic.section.infrastructure.ClassScheduleRepository;
import com.ums.asset.domain.Asset;
import com.ums.asset.infrastructure.AssetRepository;
import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.exam.infrastructure.ExamScheduleRepository;
import com.ums.organization.api.dto.RoomRequest;
import com.ums.organization.api.dto.RoomResponse;
import com.ums.organization.application.RoomService;
import com.ums.organization.application.mapper.RoomMapper;
import com.ums.organization.domain.Building;
import com.ums.organization.domain.Room;
import com.ums.organization.infrastructure.BuildingRepository;
import com.ums.organization.infrastructure.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * See {@link RoomService}. Every method runs in one transaction (@Transactional on the class):
 * when a rule fails, nothing is saved. Reads use readOnly = true, which is a little faster.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;             // the room must belong to a building that exists
    private final ClassScheduleRepository classScheduleRepository;   // delete check: classes taught in the room
    private final ExamScheduleRepository examScheduleRepository;     // delete check: exams planned in the room
    private final AssetRepository assetRepository;                   // delete check: projectors, tables … in the room
    private final RoomMapper roomMapper;                             // request → entity → response (MapStruct)

    @Override
    public RoomResponse create(RoomRequest request) {

        // 1. Same code style for everyone: " r101 " → "R101"
        String code = normalizeCode(request.code());

        // 2. The code must be free in this building (the same code in another building is OK)
        if (roomRepository.existsByBuildingIdAndCode(request.buildingId(), code)) {
            throw new BusinessException("error.room.code_exists",code);
        }

        log.info("log build id {} :",request.buildingId());

        // 3. The building must exist and not be deleted
        Building building = findBuilding(request.buildingId());

        // 4. Copy the request into a new entity, then set the building and the cleaned values
        Room room = roomMapper.toEntity(request);
        room.setBuilding(building);
        room.setCode(code);
        room.setCapacity(request.capacity());
        room.setRoomType(request.roomType());

        // 5. INSERT, then answer with the saved room (it has its id now)
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override

    @Transactional(readOnly = true)
    public RoomResponse get(Long id) {
        return roomMapper.toResponse(findRoom(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> list(String search, Long buildingId, Room.RoomType roomType, Pageable pageable) {

        String q = search == null ? "" : search.trim();   // null would break the query; "" = all

        return roomRepository.search(q, buildingId, roomType, pageable).map(roomMapper::toResponse);   // entity page → response page
    }

    @Override
    public RoomResponse update(Long id, RoomRequest request) {

        // 1. The room must exist and not be deleted
        Room room = findRoom(id);

        // 2. The new code must be free in the building; its own code is allowed (IdNot skips this room)
        String code = normalizeCode(request.code());
        if (roomRepository.existsByBuildingIdAndCodeAndIdNot(request.buildingId(), code, id)) {
            throw new BusinessException("error.room.code_exists",code);
        }

        // 3. The building must exist and not be deleted (the room can move to another building)
        Building building = findBuilding(request.buildingId());

        // 4. Copy the request onto the room, then set the building and the cleaned values
        roomMapper.update(request,room);
        room.setBuilding(building);
        room.setCode(code);
        room.setCapacity(request.capacity());
        room.setRoomType(request.roomType());

        // 5. No save() needed: the room was loaded in this transaction, so JPA writes the UPDATE at commit
        return roomMapper.toResponse(room);
    }

    @Override
    public void delete(Long id) {

        // 1. The room must exist and not be deleted already
        Room room  = findRoom(id);

        // 2. Classes, exams or assets still use the room: refuse
        if(isInUse(id)){
            throw  new BusinessException("error.room.in_use");
        }

        // 3. Soft delete: the row stays, GET no longer shows it (UPDATE at commit, like in update)
        room.setDeletedAt(OffsetDateTime.now());

    }

    /**
     * Whether the room is still used today. History (theses, events) and timetable drafts are not checked:
     * with soft delete the row stays, so they can still point to it.
     */
    private boolean isInUse(Long roomId){
        return classScheduleRepository.existsByRoomId(roomId)                              // weekly classes
                || examScheduleRepository.existsByRoomIdAndDeletedAtIsNull(roomId)         // planned exams
                || assetRepository.existsByRoomIdAndDeletedAtIsNullAndStatusNot(roomId, Asset.Status.DISPOSED);   // things in the room
    }

    /** The building, or 404 when it does not exist or was deleted. Used by create and update. */
    private Building findBuilding(Long id) {
        return buildingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.building.not_found"));
    }

    /** The room, or 404 when it does not exist or was deleted. Used by get, update and delete. */
    private Room findRoom(Long id) {
        return roomRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.room.not_found"));
    }

    /** " r101 " → "R101", so R101 and r101 can't become two rooms. Locale.ROOT: same result on every server. */
    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
