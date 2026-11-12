package com.ums.people.application.mapper;

import com.ums.people.api.dto.StaffRequest;
import com.ums.people.api.dto.StaffResponse;
import com.ums.people.api.dto.StaffUpdateRequest;
import com.ums.people.domain.Person;
import com.ums.people.domain.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct writes the code of this interface at build time (StaffMapperImpl in build/generated).
 * Fields with the same name are copied by themselves; @Mapping(ignore = true) marks the ones the service sets
 * (or the database fills), so the request can never set them.
 */
@Mapper(componentModel = "spring")
public interface StaffMapper {

    /** The person part of the form: names, gender, dob, nationalId, phone, email. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Person toPerson(StaffRequest request);

    /** Update form → the person already loaded (@MappingTarget: no new object). Names, gender, dob, contact ... */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nationalityCode", ignore = true)   // set by the service (countryService checks the code)
    @Mapping(target = "photoUrl", ignore = true)          // kept as it is: the photo has its own endpoint
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePerson(StaffUpdateRequest request, @MappingTarget Person person);

    /** The job part of the form: staffCode, departmentId, position, academicTitle, staffType, hireDate. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)     // set by the service: the new or existing login
    @Mapping(target = "person", ignore = true)     // set by the service: the new or existing person
    @Mapping(target = "status", ignore = true)     // always ACTIVE when hired (default in Staff)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Staff toStaff(StaffRequest request);

    /** Update form → the staff already loaded (@MappingTarget: no new object). Department, position, title, type, hire date. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "person", ignore = true)     // the person part: updatePerson
    @Mapping(target = "staffCode", ignore = true)  // it is the username: PUT /staff/{id}/code
    @Mapping(target = "status", ignore = true)     // POST /staff/{id}/status
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateStaff(StaffUpdateRequest request, @MappingTarget Staff staff);

    /** Staff + its person (source = "person.x" reads staff.getPerson().getX()). */
    @Mapping(target = "firstName", source = "person.firstName")
    @Mapping(target = "lastName", source = "person.lastName")
    @Mapping(target = "firstNameKh", source = "person.firstNameKh")
    @Mapping(target = "lastNameKh", source = "person.lastNameKh")
    @Mapping(target = "gender", source = "person.gender")
    @Mapping(target = "dob", source = "person.dob")
    @Mapping(target = "phone", source = "person.phone")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "photoUrl", source = "person.photoUrl")
    @Mapping(target = "personId", source = "person.id")
    StaffResponse toResponse(Staff staff);
}
