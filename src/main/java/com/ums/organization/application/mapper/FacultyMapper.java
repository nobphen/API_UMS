package com.ums.organization.application.mapper;

import com.ums.organization.api.dto.FacultyRequest;
import com.ums.organization.api.dto.FacultyResponse;
import com.ums.organization.domain.Faculty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FacultyMapper {

    FacultyResponse toResponse(Faculty faculty);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Faculty toEntity(FacultyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(FacultyRequest request, @MappingTarget Faculty faculty);
}
