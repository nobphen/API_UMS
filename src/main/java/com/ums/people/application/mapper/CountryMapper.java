package com.ums.people.application.mapper;

import com.ums.people.api.dto.CountryResponse;
import com.ums.people.domain.Country;
import org.mapstruct.Mapper;

/** Turns Country entities into CountryResponse (MapStruct writes the code). */
@Mapper(componentModel = "spring")
public interface CountryMapper {

    /** Country to CountryResponse: code, names and nationalities; the active flag is not sent. */
    CountryResponse toResponse(Country country);
}
