package com.ums.organization.application;

import com.ums.organization.api.dto.BuildingRequest;
import com.ums.organization.api.dto.BuildingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Campus buildings: create, read, change and (soft) delete. Used by BuildingController. */
public interface BuildingService {

    /**
     * Adds a building. The code is saved in capitals (" b1 " → "B1"); an empty Khmer name is saved as null.
     *
     * @param request code, name and Khmer name
     * @return the new building, with its id
     * @throws com.ums.common.exception.BusinessException error.building.code_exists when any building has the code,
     *                                                    a deleted one included
     */
    BuildingResponse create(BuildingRequest request);

    /**
     * One building.
     *
     * @param id building id
     * @return the building
     * @throws com.ums.common.exception.NotFoundException error.building.not_found when it does not exist or was deleted
     */
    BuildingResponse get(Long id);

    /**
     * One page of buildings, deleted ones left out.
     *
     * @param search   filters by code, English or Khmer name; null or empty = all
     * @param pageable page number, size and sort (?page=1&size=20&sort=name,asc)
     * @return the buildings of that page, with the totals
     */
    Page<BuildingResponse> list(String search, Pageable pageable);

    /**
     * Replaces all the building's fields with the request (code in capitals, empty Khmer name → null).
     *
     * @param id      building id
     * @param request the new values; the building may keep its own code
     * @return the building after the change
     * @throws com.ums.common.exception.NotFoundException error.building.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.building.code_exists when another building has the code
     */
    BuildingResponse update(Long id, BuildingRequest request);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its code can't be used again.
     *
     * @param id building id
     * @throws com.ums.common.exception.NotFoundException error.building.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.building.has_rooms while rooms belong to it
     */
    void delete(Long id);
}
