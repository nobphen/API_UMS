package com.ums.people.application.mapper;

import com.ums.people.api.dto.AddressResponse;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import com.ums.people.domain.Person;
import com.ums.people.domain.Student;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    Person toPerson(StudentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "yearLevel", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Student toStudent(StudentRequest request);

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
