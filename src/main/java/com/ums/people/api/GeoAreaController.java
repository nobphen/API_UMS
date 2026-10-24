package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.security.RequirePermission;
import com.ums.people.PeoplePermissions;
import com.ums.people.api.dto.GeoAreaRequest;
import com.ums.people.api.dto.GeoAreaResponse;
import com.ums.people.api.dto.GeoAreaUpdateRequest;
import com.ums.people.application.GeoAreaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Province → district → commune → village dropdowns for any logged-in user; changes need GEO_AREA_MANAGE. */
@RestController
@RequestMapping("/api/v1/geo-areas")
@RequiredArgsConstructor
public class GeoAreaController {

    private final GeoAreaService geoAreaService;

    /** No parentId: the 25 provinces. With parentId: the districts, communes or villages inside it. */
    @GetMapping
    public ApiResponse<List<GeoAreaResponse>> list(@RequestParam(required = false) Long parentId,
                                                   @RequestParam(required = false) String q,
                                                   @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ApiResponse.ok(geoAreaService.list(parentId, q, includeInactive));
    }

    @GetMapping("/{id}")
    public ApiResponse<GeoAreaResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(geoAreaService.get(id));
    }

    @GetMapping("/{id}/path")
    public ApiResponse<List<GeoAreaResponse>> path(@PathVariable Long id) {
        return ApiResponse.ok(geoAreaService.path(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(PeoplePermissions.GEO_AREA_MANAGE)
    public ApiResponse<GeoAreaResponse> create(@Valid @RequestBody GeoAreaRequest request) {
        return ApiResponse.ok(geoAreaService.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission(PeoplePermissions.GEO_AREA_MANAGE)
    public ApiResponse<GeoAreaResponse> update(@PathVariable Long id, @Valid @RequestBody GeoAreaUpdateRequest request) {
        return ApiResponse.ok(geoAreaService.update(id, request));
    }
}
