package com.ums.people.application.mapper;

import com.ums.people.api.dto.CountryResponse;
import com.ums.people.domain.Country;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryResponse toResponse(Country country);
}
