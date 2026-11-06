package com.ums.people.application.mapper;

import com.ums.people.api.dto.AddressResponse;
import com.ums.people.api.dto.GeoAreaResponse;
import com.ums.people.domain.GeoArea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Turns GeoArea entities into GeoAreaResponse and AddressResponse.Area (MapStruct writes the code). */
@Mapper(componentModel = "spring")
public interface GeoAreaMapper {

    /** GeoArea to GeoAreaResponse; parentId is taken from parent.id (null for a province). */
    @Mapping(target = "parentId", source = "parent.id")
    GeoAreaResponse toResponse(GeoArea area);

    /** GeoArea to one level of an AddressResponse: id, code, names and unit words; level, parent and active are not sent. */
    AddressResponse.Area toArea(GeoArea area);
}
