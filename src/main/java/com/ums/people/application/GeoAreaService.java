package com.ums.people.application;

import com.ums.people.api.dto.GeoAreaRequest;
import com.ums.people.api.dto.GeoAreaResponse;
import com.ums.people.api.dto.GeoAreaUpdateRequest;
import java.util.List;

/** Cambodia's provinces, districts, communes and villages, kept up to date by admins. */
public interface GeoAreaService {

    /** Provinces when parentId is empty, otherwise the areas inside parentId; q filters by Khmer or English name. */
    List<GeoAreaResponse> list(Long parentId, String q, boolean includeInactive);

    GeoAreaResponse get(Long id);

    /** The area and every level above it, province first: fills the dropdowns when an address is edited. */
    List<GeoAreaResponse> path(Long id);

    GeoAreaResponse create(GeoAreaRequest request);

    GeoAreaResponse update(Long id, GeoAreaUpdateRequest request);
}
