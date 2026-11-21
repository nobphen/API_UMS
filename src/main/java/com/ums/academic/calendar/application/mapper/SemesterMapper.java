package com.ums.academic.calendar.application.mapper;

import com.ums.academic.calendar.api.dto.SemesterRequest;
import com.ums.academic.calendar.api.dto.SemesterResponse;
import com.ums.academic.calendar.domain.Semester;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Semester request → entity → response (MapStruct writes the code at build time).
 * Fields with the same name are copied on their own: name, nameKh, semesterNo, startDate, endDate,
 * registrationStart, registrationEnd.
 * academicYear is NOT copied from the request (the request only has its id): the service loads it and sets it.
 */
@Mapper(componentModel = "spring")
public interface SemesterMapper {

    /** Entity → what the client gets back. The year's id and name come from semester.academicYear. */
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearName", source = "academicYear.name")
    SemesterResponse toResponse(Semester semester);

    /** Create form → a new entity, not saved yet (current = false, academicYear set by the service). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Semester toEntity(SemesterRequest request);

    /** Update form → the semester already loaded (@MappingTarget: no new object). current stays as it is. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(SemesterRequest request, @MappingTarget Semester semester);
}
