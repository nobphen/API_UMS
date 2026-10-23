package com.ums.people.application.mapper;

import com.ums.people.api.dto.AddressResponse;
import com.ums.people.api.dto.GeoAreaResponse;
import com.ums.people.domain.GeoArea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GeoAreaMapper {

    @Mapping(target = "parentId", source = "parent.id")
    GeoAreaResponse toResponse(GeoArea area);

    AddressResponse.Area toArea(GeoArea area);
}
