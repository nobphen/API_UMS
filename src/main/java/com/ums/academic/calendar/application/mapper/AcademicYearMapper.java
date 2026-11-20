package com.ums.academic.calendar.application.mapper;

import com.ums.academic.calendar.api.dto.AcademicYearRequest;
import com.ums.academic.calendar.api.dto.AcademicYearResponse;
import com.ums.academic.calendar.domain.AcademicYear;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Academic year request → entity → response (MapStruct writes the code at build time).
 * Fields with the same name are copied on their own: name, startDate, endDate (and current in the response).
 * id, current, deletedAt, createdAt and updatedAt are never taken from a request.
 */
@Mapper(componentModel = "spring")
public interface AcademicYearMapper {

    /** Entity → what the client gets back: id, name, startDate, endDate, current. */
    AcademicYearResponse toResponse(AcademicYear year);

    /** Create form → a new entity, not saved yet (current = false). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AcademicYear toEntity(AcademicYearRequest request);

    /** Update form → the year already loaded (@MappingTarget: no new object). current stays as it is. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(AcademicYearRequest request, @MappingTarget AcademicYear year);
}
