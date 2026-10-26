package com.ums.people.api.dto;

import com.ums.common.response.PhoneDisplay;
import com.ums.people.domain.Person;
import java.time.LocalDate;
import java.util.List;

public record PersonResponse(
        Long id,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        String nationalityCode,
        String nationalId,
        @PhoneDisplay String phone,
        String email,
        List<AddressResponse> addresses,
        String photoUrl) {
}
