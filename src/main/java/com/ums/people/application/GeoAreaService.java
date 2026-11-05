package com.ums.people.application;

import com.ums.people.api.dto.GeoAreaRequest;
import com.ums.people.api.dto.GeoAreaResponse;
import com.ums.people.api.dto.GeoAreaUpdateRequest;
import java.util.List;

/** Cambodia's provinces, districts, communes and villages, kept up to date by admins. Used by GeoAreaController. */
public interface GeoAreaService {

    /**
     * One dropdown list, ordered by English name.
     *
     * @param parentId        null = the provinces; otherwise the areas directly inside this area
     * @param q               filters by Khmer or English name; null or empty = all
     * @param includeInactive true also returns switched-off areas
     * @return the areas of that level
     */
    List<GeoAreaResponse> list(Long parentId, String q, boolean includeInactive);

    /**
     * One area, active or not.
     *
     * @param id area id
     * @return the area
     * @throws com.ums.common.exception.NotFoundException error.geo.not_found when it does not exist
     */
    GeoAreaResponse get(Long id);

    /**
     * The area and every level above it, province first: fills the dropdowns when only one area id is known.
     *
     * @param id area id, any level
     * @return province, district, commune, village, up to the given area (a province gives a list of one)
     * @throws com.ums.common.exception.NotFoundException error.geo.not_found when it does not exist
     */
    List<GeoAreaResponse> path(Long id);

    /**
     * Adds an area with its official code.
     *
     * @param request code, level, parent, Khmer and English name and unit
     * @return the new area, with its id
     * @throws com.ums.common.exception.NotFoundException error.geo.not_found when the parent does not exist
     * @throws com.ums.common.exception.BusinessException error.geo.province_has_no_parent, error.geo.needs_parent,
     *                                                    error.geo.must_be_inside when the parent is wrong for the level;
     *                                                    error.geo.code_length, error.geo.code_prefix when the code does
     *                                                    not fit the level or the parent; error.geo.code_used when another
     *                                                    area has the code
     */
    GeoAreaResponse create(GeoAreaRequest request);

    /**
     * Renames an area or switches it off/on. The code, level and parent never change.
     *
     * @param id      area id
     * @param request the new names and units; active null = unchanged
     * @return the area after the change
     * @throws com.ums.common.exception.NotFoundException error.geo.not_found when it does not exist
     */
    GeoAreaResponse update(Long id, GeoAreaUpdateRequest request);
}
