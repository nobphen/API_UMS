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

/**
 * Cambodia's provinces, districts, communes and villages (one table, each area points to its parent).
 * Reading is open to every logged-in user (the province → district → commune → village dropdowns of an address);
 * creating and changing need GEO_AREA_MANAGE. There is no delete: an old area is switched off (active = false).
 */
@RestController
@RequestMapping("/api/v1/geo-areas")
@RequiredArgsConstructor
public class GeoAreaController {

    private final GeoAreaService geoAreaService;

    /**
     * One dropdown list, ordered by English name, inactive areas left out.
     * No parentId: the 25 provinces. With parentId: the districts, communes or villages inside it.
     * q matches the Khmer or English name; includeInactive=true also shows switched-off areas (admin page).
     * <pre>GET /api/v1/geo-areas                      → provinces
     * GET /api/v1/geo-areas?parentId=1             → districts of province 1
     * GET /api/v1/geo-areas?parentId=26&q=banteay  → communes of district 26 named like "banteay"</pre>
     */
    @GetMapping
    public ApiResponse<List<GeoAreaResponse>> list(@RequestParam(required = false) Long parentId,
                                                   @RequestParam(required = false) String q,
                                                   @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ApiResponse.ok(geoAreaService.list(parentId, q, includeInactive));
    }

    /**
     * One area, active or not; 404 when it does not exist.
     * <pre>GET /api/v1/geo-areas/236</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<GeoAreaResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(geoAreaService.get(id));
    }

    /**
     * The area and every level above it, province first. With one id (e.g. from a search box) the frontend can
     * fill all the dropdowns above it.
     * <pre>GET /api/v1/geo-areas/236/path  → [Banteay Meanchey, Mongkol Borey, Banteay Neang]</pre>
     */
    @GetMapping("/{id}/path")
    public ApiResponse<List<GeoAreaResponse>> path(@PathVariable Long id) {
        return ApiResponse.ok(geoAreaService.path(id));
    }

    /**
     * Adds an area, e.g. after a sub-decree creates one. The code is the official code: 2 digits per level
     * (province 2, district 4, commune 6, village 8), starting with the parent's code. A province has no parentId.
     * <pre>POST /api/v1/geo-areas  {"code": "0199", "level": "DISTRICT", "parentId": 1, "nameKh": "…", "nameEn": "…",
     *                          "unitKh": "ស្រុក", "unitEn": "District"}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(PeoplePermissions.GEO_AREA_MANAGE)
    public ApiResponse<GeoAreaResponse> create(@Valid @RequestBody GeoAreaRequest request) {
        return ApiResponse.ok(geoAreaService.create(request));
    }

    /**
     * Renames an area or switches it off/on. The code, level and parent never change (they are not in the body):
     * when areas are merged or moved, add the new area and switch the old one off.
     * <pre>PUT /api/v1/geo-areas/26  {"nameKh": "…", "nameEn": "Mongkol Borey", "unitKh": "ស្រុក", "unitEn": "District",
     *                            "active": false}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(PeoplePermissions.GEO_AREA_MANAGE)
    public ApiResponse<GeoAreaResponse> update(@PathVariable Long id, @Valid @RequestBody GeoAreaUpdateRequest request) {
        return ApiResponse.ok(geoAreaService.update(id, request));
    }
}
