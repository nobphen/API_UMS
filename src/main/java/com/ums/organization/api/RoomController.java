package com.ums.organization.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.organization.OrganizationPermissions;
import com.ums.organization.api.dto.RoomRequest;
import com.ums.organization.api.dto.RoomResponse;
import com.ums.organization.application.RoomService;
import com.ums.organization.domain.Room;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Rooms in campus buildings (R101, Computer Lab 1 …), where classes and exams take place.
 * Reading is open to every logged-in user (lists and dropdowns, e.g. choosing a room for a class);
 * creating, changing and deleting need ROOM_MANAGE (one permission for buildings and rooms).
 */
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * Adds a room to a building. The code is saved in capitals (r101 → R101) and must not be used by another
     * room in the same building, deleted ones included.
     * <pre>POST /api/v1/rooms  {"buildingId": 1, "code": "R101", "capacity": 40, "roomType": "LECTURE"}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(OrganizationPermissions.ROOM_MANAGE)
    public ApiResponse<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        return ApiResponse.created(roomService.create(request));
    }

    /**
     * One room; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/rooms/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(roomService.get(id));
    }

    /**
     * One page of rooms, deleted ones left out; search matches the code. buildingId and roomType are optional filters.
     * <pre>GET /api/v1/rooms?search=r1&buildingId=1&roomType=LAB&page=1&size=20&sort=code,asc</pre>
     * Without page/size/sort: the first 20, ordered by code.
     */
    @GetMapping
    public ApiResponse<PageResponse<RoomResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Room.RoomType roomType,
            @PageableDefault(sort = "code", size = 20) Pageable pageable
    ) {
        return ApiResponse.page(roomService.list(search, buildingId, roomType, pageable));
    }


    /**
     * Replaces all the room's fields (send every field, like for create). The room can move to another building.
     * <pre>PUT /api/v1/rooms/1  {"buildingId": 1, "code": "R101", "capacity": 45, "roomType": "LECTURE"}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(OrganizationPermissions.ROOM_MANAGE)
    public ApiResponse<RoomResponse> update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return ApiResponse.updated(roomService.update(id, request));
    }

    /**
     * Soft delete: the row stays for history, but the room is no longer listed.
     * Refused (400) while class schedules, exam schedules or assets still use it.
     * <pre>DELETE /api/v1/rooms/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(OrganizationPermissions.ROOM_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ApiResponse.deleted();
    }


}
