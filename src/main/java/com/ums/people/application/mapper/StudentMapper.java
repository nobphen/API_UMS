package com.ums.people.application.mapper;

import com.ums.people.api.dto.AddressResponse;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import com.ums.people.api.dto.StudentUpdateRequest;
import com.ums.people.domain.Person;
import com.ums.people.domain.Student;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct writes the code of this interface at build time (StudentMapperImpl in target/generated-sources).
 * Fields with the same name are copied by themselves; @Mapping(ignore = true) marks the ones the service sets
 * (or the database fills), so the request can never set them.
 */
@Mapper(componentModel = "spring")
public interface StudentMapper {



    /** The person part of the form: names, gender, dob, nationalId, phone, email. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Person toPerson(StudentRequest request);

    /** Update form → the person already loaded (@MappingTarget: no new object). Names, gender, dob, photo ... */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nationalityCode", ignore = true)   // set by the service (countryService checks the code)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePerson(StudentUpdateRequest request, @MappingTarget Person person);

    /** The study part of the form: studentCode, programId, batchId, admissionDate, expectedGraduationYear. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "yearLevel", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Student toStudent(StudentRequest request);

    /** Student + its person (source = "student.person.x" reads student.getPerson().getX()) + the addresses. */
    @Mapping(target = "firstName", source = "student.person.firstName")
    @Mapping(target = "lastName", source = "student.person.lastName")
    @Mapping(target = "firstNameKh", source = "student.person.firstNameKh")
    @Mapping(target = "lastNameKh", source = "student.person.lastNameKh")
    @Mapping(target = "gender", source = "student.person.gender")
    @Mapping(target = "dob", source = "student.person.dob")
    @Mapping(target = "phone", source = "student.person.phone")
    @Mapping(target = "email", source = "student.person.email")
    @Mapping(target = "personId", source = "student.person.id")
    StudentResponse toResponse(Student student, List<AddressResponse> addresses);
}
