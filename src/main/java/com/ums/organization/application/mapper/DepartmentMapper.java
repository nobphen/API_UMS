package com.ums.organization.application.mapper;

import com.ums.organization.api.dto.DepartmentRequest;
import com.ums.organization.api.dto.DepartmentResponse;
import com.ums.organization.domain.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Copies fields with the same name between the request, the entity and the response (MapStruct writes the code).
 * Fields with no match in the request are ignored on purpose: id (made by the database), faculty (set by the
 * service after findFaculty) and deletedAt (set only by delete).
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    /** Entity → JSON answer. */
    @Mapping(target = "facultyId", source = "faculty.id")   // entity has Faculty, response has Long
    DepartmentResponse toResponse(Department department);

    /** Request → new entity, for create. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "faculty", ignore = true)              // set by the service (findFaculty)
    @Mapping(target = "deletedAt", ignore = true)
    Department toEntity(DepartmentRequest request);

    /** Request → the department already loaded (@MappingTarget: no new object), for update. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "faculty", ignore = true)              // set by the service (findFaculty)
    @Mapping(target = "deletedAt", ignore = true)
    void update(DepartmentRequest request, @MappingTarget Department department);
}
