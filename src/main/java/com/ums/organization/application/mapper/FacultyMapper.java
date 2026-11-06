package com.ums.organization.application.mapper;

import com.ums.organization.api.dto.FacultyRequest;
import com.ums.organization.api.dto.FacultyResponse;
import com.ums.organization.domain.Faculty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** MapStruct mapper (code made at build time): faculty request / entity / response. */
@Mapper(componentModel = "spring")
public interface FacultyMapper {

    /** Entity → JSON answer. */
    FacultyResponse toResponse(Faculty faculty);

    /** Request → new entity, for create. id, deletedAt, createdAt, updatedAt are ignored: the database and JPA fill them. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Faculty toEntity(FacultyRequest request);

    /** Request → the faculty already loaded (@MappingTarget: no new object), for update. Same fields ignored as toEntity. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(FacultyRequest request, @MappingTarget Faculty faculty);
}
