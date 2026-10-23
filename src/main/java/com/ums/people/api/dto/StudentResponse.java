package com.ums.people.api.dto;

import com.ums.people.domain.Person;
import com.ums.people.domain.Student;
import java.time.LocalDate;
import java.util.List;

public record StudentResponse(
        Long id,
        String studentCode,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        String phone,
        String email,
        Long programId,
        Long batchId,
        int yearLevel,
        LocalDate admissionDate,
        Integer expectedGraduationYear,
        Student.Status status,
        Long userId,
        Long personId,
        List<AddressResponse> addresses) {
}
