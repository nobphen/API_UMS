package com.ums.organization.application.mapper;

import com.ums.organization.api.dto.RoomRequest;
import com.ums.organization.api.dto.RoomResponse;
import com.ums.organization.domain.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Fields with no match in the request are ignored on purpose: id (made by the database), building (set by the
 * service, the request only has buildingId), and the timestamps (set by JPA auditing and the soft delete).
 */
@Mapper(componentModel = "spring")
public interface RoomMapper {

    /** Entity → JSON answer. */
    @Mapping(target = "buildingId",source = "building.id")   // entity has Building, response has Long
    RoomResponse toResponse(Room room);


    /** Request → new entity, for create. */
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "building",ignore = true)              // set by the service (findBuilding)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "deletedAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    Room toEntity(RoomRequest request);


    /** Request → the room already loaded (@MappingTarget: no new object), for update. */
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "building",ignore = true)              // set by the service (findBuilding)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "deletedAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    void update(RoomRequest request , @MappingTarget Room room);
}
