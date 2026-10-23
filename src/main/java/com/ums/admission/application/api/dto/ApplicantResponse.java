package com.ums.admission.application.api.dto;

import com.ums.people.domain.Person;
import java.time.LocalDate;

public record ApplicantResponse(
        Long id,
        Long personId,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        String phone,
        String email,
        String highSchool,
        Integer grade12Year,
        String grade12Result,
        Long userId) {
}
