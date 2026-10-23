package com.ums.admission.application.api.dto;

import com.ums.people.api.dto.AddressRequest;
import com.ums.people.domain.Person;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** An applicant's personal details (stored as a person) and grade 12 details. */
public record ApplicantRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 80) String firstNameKh,
        @Size(max = 80) String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        @Pattern(regexp = "^[A-Z]{2}$", message = "must be a country code such as KH") String nationalityCode,   // empty = KH
        @Size(max = 50) String nationalId,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        @Valid List<AddressRequest> addresses,   // birth place, permanent and current address

        @Size(max = 200) String highSchool,
        Integer grade12Year,
        @Size(max = 10) String grade12Result) {
}
