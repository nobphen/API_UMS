package com.ums.people.api.dto;

import com.ums.common.response.PhoneDisplay;
import com.ums.people.domain.Person;

/** A guardian (parent) of a student sent back to the client. userId is null when the guardian has no login. */
public record GuardianResponse(
        Long id,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        @PhoneDisplay String phone,
        String email,
        String occupation,
        Long userId) {
}
