package com.ums.organization.application.mapper;

import com.ums.organization.api.dto.BuildingRequest;
import com.ums.organization.api.dto.BuildingResponse;
import com.ums.organization.domain.Building;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BuildingMapper {

    /** Entity → JSON answer. */
    BuildingResponse toResponse(Building building);

    /** Request → new entity, for create. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Building toEntity(BuildingRequest request);

    /** Request → the building already loaded (@MappingTarget: no new object), for update. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(BuildingRequest request, @MappingTarget Building building);
}
