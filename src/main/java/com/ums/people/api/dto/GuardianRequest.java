package com.ums.people.api.dto;

import com.ums.people.domain.Person;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record GuardianRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 80) String firstNameKh,
        @Size(max = 80) String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        @Size(max = 50) String nationalId,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        @Valid List<AddressRequest> addresses,   // birth place, permanent and current address

        @Size(max = 100) String occupation) {
}
