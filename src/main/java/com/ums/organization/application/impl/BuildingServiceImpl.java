package com.ums.organization.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.organization.api.dto.BuildingRequest;
import com.ums.organization.api.dto.BuildingResponse;
import com.ums.organization.application.BuildingService;
import com.ums.organization.application.mapper.BuildingMapper;
import com.ums.organization.domain.Building;
import com.ums.organization.infrastructure.BuildingRepository;
import com.ums.organization.infrastructure.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * See {@link BuildingService}. Every method runs in one transaction (@Transactional on the class):
 * when a rule fails, nothing is saved. Reads use readOnly = true, which is a little faster.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;   // to check a building is empty before deleting it
    private final BuildingMapper buildingMapper;   // request → entity → response (MapStruct)

    @Override
    public BuildingResponse create(BuildingRequest request) {

        // 1. Same code style for everyone: " b1 " → "B1"
        String code = normalizeCode(request.code());

        // 2. The code must be free (the database keeps it UNIQUE, deleted buildings included)
        if (buildingRepository.existsByCode(code)) {
            throw new BusinessException("error.building.code_exists", code);
        }

        // 3. Copy the request into a new entity, then set the cleaned values
        Building building = buildingMapper.toEntity(request);
        building.setCode(code);
        building.setNameKh(blankToNull(request.nameKh()));

        // 4. INSERT, then answer with the saved building (it has its id now)
        return buildingMapper.toResponse(buildingRepository.save(building));
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingResponse get(Long id) {
        return buildingMapper.toResponse(findBuilding(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuildingResponse> list(String search, Pageable pageable) {
        String q = search == null ? "" : search.trim();   // null would break the query; "" = all
        return buildingRepository.search(q, pageable).map(buildingMapper::toResponse);   // entity page → response page
    }

    @Override
    public BuildingResponse update(Long id, BuildingRequest request) {

        // 1. The building must exist and not be deleted
        Building building = findBuilding(id);

        // 2. The new code must be free; its own code is allowed (IdNot skips this building)
        String code = normalizeCode(request.code());
        if (buildingRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessException("error.building.code_exists", code);
        }

        // 3. Copy the request onto the building, then set the cleaned values
        buildingMapper.update(request, building);
        building.setCode(code);
        building.setNameKh(blankToNull(request.nameKh()));

        // 4. No save() needed: the building was loaded in this transaction, so JPA writes the UPDATE at commit
        return buildingMapper.toResponse(building);
    }

    @Override
    public void delete(Long id) {

        // 1. The building must exist and not be deleted already
        Building building = findBuilding(id);

        // 2. Rooms still point to it: deleting it would leave them without a building
        if (roomRepository.existsByBuildingIdAndDeletedAtIsNull(id)) {
            throw new BusinessException("error.building.has_rooms");
        }

        // 3. Soft delete: the row stays, GET no longer shows it (UPDATE at commit, like in update)
        building.setDeletedAt(OffsetDateTime.now());
    }

    /** The building, or 404 when it does not exist or was deleted. Used by get, update and delete. */
    private Building findBuilding(Long id) {
        return buildingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.building.not_found"));
    }

    /** " b1 " → "B1", so B1 and b1 can't become two buildings. Locale.ROOT: same result on every server. */
    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** "" or "   " → null, so an empty Khmer name is saved as "no value", not as an empty text. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
