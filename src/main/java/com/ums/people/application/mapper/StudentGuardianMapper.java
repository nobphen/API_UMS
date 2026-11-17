package com.ums.people.application.mapper;

import com.ums.people.api.dto.StudentGuardianRequest;
import com.ums.people.api.dto.StudentGuardianResponse;
import com.ums.people.api.dto.StudentGuardianUpdateRequest;
import com.ums.people.domain.Guardian;
import com.ums.people.domain.StudentGuardian;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct writes the code of this interface at build time (StudentGuardianMapperImpl in build/generated).
 * The response has 2 sources: the link (relationship, primary) and the guardian with its person (names, phone).
 * With 2 sources every field names its source: "link.x" or "guardian.x".
 */
@Mapper(componentModel = "spring")
public interface StudentGuardianMapper {

    /**
     * Link form → a NEW link (relationship). Not from the form: the id (student id is in the URL)
     * and primary (the service decides it: the first guardian is always primary).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "primary", ignore = true)
    StudentGuardian toEntity(StudentGuardianRequest request);

    /**
     * Update form → the link already loaded (@MappingTarget: no new object). Relationship only.
     * Not from the form: the id (never changes) and primary (the service decides it: a student must keep one primary).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "primary", ignore = true)
    void update(StudentGuardianUpdateRequest request, @MappingTarget StudentGuardian link);

    /** Link + guardian → one response. The key is inside the @EmbeddedId, so the ids are link.id.studentId / guardianId. */
    @Mapping(target = "studentId", source = "link.id.studentId")
    @Mapping(target = "guardianId", source = "link.id.guardianId")
    @Mapping(target = "relationship", source = "link.relationship")
    @Mapping(target = "primary", source = "link.primary")
    @Mapping(target = "firstName", source = "guardian.person.firstName")
    @Mapping(target = "lastName", source = "guardian.person.lastName")
    @Mapping(target = "firstNameKh", source = "guardian.person.firstNameKh")
    @Mapping(target = "lastNameKh", source = "guardian.person.lastNameKh")
    @Mapping(target = "phone", source = "guardian.person.phone")
    @Mapping(target = "occupation", source = "guardian.occupation")
    StudentGuardianResponse toResponse(StudentGuardian link, Guardian guardian);
}
