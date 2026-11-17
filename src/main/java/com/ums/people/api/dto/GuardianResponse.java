package com.ums.people.api.dto;

import com.ums.common.response.PhoneDisplay;
import com.ums.people.domain.Person;
import java.time.LocalDate;

/**
 * A guardian (parent) of a student sent back to the client. userId is null when the guardian has no login.
 * personId is for the person endpoints: PUT /persons/{personId}/addresses (and the photo later).
 */
public record GuardianResponse(
        Long id,
        Long personId,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        String nationalityCode,
        String nationalId,           // the edit form needs it, so PUT does not wipe it
        @PhoneDisplay String phone,
        String email,
        String occupation,
        Long userId) {
}
