package com.ums.people.application.mapper;

import com.ums.people.api.dto.AddressResponse;
import com.ums.people.api.dto.MyProfileUpdateRequest;
import com.ums.people.api.dto.PersonResponse;
import com.ums.people.domain.Person;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct writes the code of this interface at build time (PersonMapperImpl in build/generated).
 * Used by "my profile" (GET / PUT /persons/me).
 */
@Mapper(componentModel = "spring")
public interface PersonMapper {

    /**
     * Person + its addresses → one response. The addresses come from PersonAddressService.getByPerson
     * (they need the full area text), so they are a second source.
     */
    @Mapping(target = "id", source = "person.id")
    @Mapping(target = "addresses", source = "addresses")
    PersonResponse toResponse(Person person, List<AddressResponse> addresses);

    /**
     * My profile form → the person already loaded (@MappingTarget: no new object). ONLY phone and email:
     * ignoreByDefault = every other field (names, national ID ...) is never touched, even if a field is added later.
     * Person.setPhone saves the phone as +855…; the service trims the email.
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "email", source = "email")
    void updateContact(MyProfileUpdateRequest request, @MappingTarget Person person);
}
