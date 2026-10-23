package com.ums.people.api.dto;

import com.ums.people.domain.Person;

public record GuardianResponse(
        Long id,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        String phone,
        String email,
        String occupation,
        Long userId) {
}
