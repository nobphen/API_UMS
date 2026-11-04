package com.ums.organization.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.response.PageResponse;
import com.ums.common.security.RequirePermission;
import com.ums.organization.OrganizationPermissions;
import com.ums.organization.api.dto.BuildingRequest;
import com.ums.organization.api.dto.BuildingResponse;
import com.ums.organization.application.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Campus buildings (Building A, Science Building …), the place of every room.
 * Reading is open to every logged-in user (lists and dropdowns, e.g. choosing a building for a room);
 * creating, changing and deleting need ROOM_MANAGE (one permission for buildings and rooms).
 */
@RestController
@RequestMapping("/api/v1/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    /**
     * Adds a building. The code is saved in capitals (b1 → B1) and must not be used by another
     * building, deleted ones included.
     * <pre>POST /api/v1/buildings  {"code": "B1", "name": "Building A", "nameKh": "…"}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(OrganizationPermissions.ROOM_MANAGE)
    public ApiResponse<BuildingResponse> create(@Valid @RequestBody BuildingRequest request) {
        return ApiResponse.created(buildingService.create(request));
    }

    /**
     * One page of buildings, deleted ones left out; search matches the code, English or Khmer name.
     * <pre>GET /api/v1/buildings?search=b1&page=1&size=20&sort=name,asc</pre>
     * Without page/size/sort: the first 20, ordered by name.
     */
    @GetMapping
    public ApiResponse<PageResponse<BuildingResponse>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.page(buildingService.list(search, pageable));
    }

    /**
     * One building; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/buildings/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<BuildingResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(buildingService.get(id));
    }

    /**
     * Replaces all the building's fields (send every field, like for create).
     * <pre>PUT /api/v1/buildings/1  {"code": "B1", "name": "Building A1", "nameKh": "…"}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(OrganizationPermissions.ROOM_MANAGE)
    public ApiResponse<BuildingResponse> update(@PathVariable Long id, @Valid @RequestBody BuildingRequest request) {
        return ApiResponse.updated(buildingService.update(id, request));
    }

    /**
     * Soft delete: the row stays for history, but the building is no longer listed.
     * Refused (400) while rooms still belong to it.
     * <pre>DELETE /api/v1/buildings/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(OrganizationPermissions.ROOM_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return ApiResponse.deleted();
    }
}
