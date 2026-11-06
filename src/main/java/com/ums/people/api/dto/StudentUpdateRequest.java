package com.ums.people.api.dto;

import com.ums.people.domain.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Changes a student's personal details and batch. Not here: studentCode (it is the username), programId
 * (another program = POST /students/{id}/programs), yearLevel (PUT /{id}/year-level), status (POST /{id}/status),
 * addresses (PUT /persons/{personId}/addresses).
 */
public record StudentUpdateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 80) String firstNameKh,
        @Size(max = 80) String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        @Pattern(regexp = "^[A-Z]{2}$", message = "{validation.country_code}") String nationalityCode,   // empty = KH
        @Size(max = 50) String nationalId,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        String photoUrl,

        Long batchId,
        Integer expectedGraduationYear) {
}
