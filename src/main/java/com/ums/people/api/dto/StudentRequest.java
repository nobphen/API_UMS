package com.ums.people.api.dto;

import com.ums.people.domain.Person;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * The registrar office adds a student; a login account is made with the student code as username.
 * For someone who already has a login (e.g. a lecturer starting a PhD), pass existingUserId: no new login is made,
 * and their stored personal details are used.
 */
public record StudentRequest(
        Long existingUserId,

        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{validation.username_chars}")
        String studentCode,   // e.g. ST2026001; also the username

        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 80) String firstNameKh,
        @Size(max = 80) String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        @Pattern(regexp = "^[A-Z]{2}$", message = "{validation.country_code}") String nationalityCode,   // empty = KH
        @Size(max = 50) String nationalId,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,   // needed for "Forgot password?"
        @Valid List<AddressRequest> addresses,   // birth place, permanent and current address

        @NotNull Long programId,
        Long batchId,
        @NotNull LocalDate admissionDate,
        @Min(1) @Max(10) Integer yearLevel,   // empty = 1; higher for a transfer student
        Integer expectedGraduationYear) {
}
