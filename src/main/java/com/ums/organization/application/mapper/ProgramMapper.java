package com.ums.organization.application.mapper;

import com.ums.organization.api.dto.ProgramRequest;
import com.ums.organization.api.dto.ProgramResponse;
import com.ums.organization.domain.Program;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Copies fields with the same name between the request, the entity and the response (MapStruct writes the code).
 * Fields with no match in the request are ignored on purpose: id (made by the database), department (set by the
 * service after findDepartment) and deletedAt (set only by delete).
 * Note: an empty status in the request is copied as null; the service must turn it into ACTIVE (status is NOT NULL).
 */
@Mapper(componentModel = "spring")
public interface ProgramMapper {

    /** Entity → JSON answer. */
    @Mapping(target = "departmentId", source = "department.id")   // entity has Department, response has Long
    ProgramResponse toResponse(Program program);

    /** Request → new entity, for create. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)                 // set by the service (findDepartment)
    @Mapping(target = "deletedAt", ignore = true)
    Program toEntity(ProgramRequest request);

    /** Request → the program already loaded (@MappingTarget: no new object), for update. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)                 // set by the service (findDepartment)
    @Mapping(target = "deletedAt", ignore = true)
    void update(ProgramRequest request, @MappingTarget Program program);
}
