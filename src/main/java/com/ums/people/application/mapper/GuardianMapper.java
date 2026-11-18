package com.ums.people.application.mapper;

import com.ums.people.api.dto.GuardianRequest;
import com.ums.people.api.dto.GuardianResponse;
import com.ums.people.api.dto.GuardianUpdateRequest;
import com.ums.people.domain.Guardian;
import com.ums.people.domain.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct writes the code of this interface at build time (GuardianMapperImpl in build/generated).
 * Fields with the same name are copied by themselves; @Mapping(ignore = true) marks the ones the service sets
 * (or the database fills), so the request can never set them.
 */
@Mapper(componentModel = "spring")
public interface GuardianMapper {

    /** The person part of the form: names, gender, dob, nationalId, phone, email. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nationalityCode", ignore = true)   // set by the service (countryService checks the code)
    @Mapping(target = "nationalId", ignore = true)        // set by the service (personService checks the ID)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Person toPerson(GuardianRequest request);

    /** Update form → the person already loaded (@MappingTarget: no new object). Names, gender, dob, contact. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nationalityCode", ignore = true)   // set by the service (countryService checks the code)
    @Mapping(target = "nationalId", ignore = true)        // set by the service (personService checks the ID)
    @Mapping(target = "photoUrl", ignore = true)          // kept as it is: the photo has its own endpoint
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePerson(GuardianUpdateRequest request, @MappingTarget Person person);

    /** The guardian part of the form: occupation. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)     // version 1: a guardian has no login (null)
    @Mapping(target = "person", ignore = true)     // set by the service: the new person
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Guardian toGuardian(GuardianRequest request);

    /** Update form → the guardian already loaded (@MappingTarget: no new object). Occupation. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "person", ignore = true)     // the person part: updatePerson
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateGuardian(GuardianUpdateRequest request, @MappingTarget Guardian guardian);

    /** Guardian + its person (source = "person.x" reads guardian.getPerson().getX()). */
    @Mapping(target = "personId", source = "person.id")
    @Mapping(target = "firstName", source = "person.firstName")
    @Mapping(target = "lastName", source = "person.lastName")
    @Mapping(target = "firstNameKh", source = "person.firstNameKh")
    @Mapping(target = "lastNameKh", source = "person.lastNameKh")
    @Mapping(target = "gender", source = "person.gender")
    @Mapping(target = "dob", source = "person.dob")
    @Mapping(target = "nationalityCode", source = "person.nationalityCode")
    @Mapping(target = "nationalId", source = "person.nationalId")
    @Mapping(target = "phone", source = "person.phone")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "photoUrl", source = "person.photoUrl")
    GuardianResponse toResponse(Guardian guardian);
}
