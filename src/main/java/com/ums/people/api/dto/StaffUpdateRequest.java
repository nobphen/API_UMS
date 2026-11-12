package com.ums.people.api.dto;

import com.ums.common.util.Emails;
import com.ums.people.domain.Person;
import com.ums.people.domain.Staff;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Changes a staff member's personal details and job details. Not here: staffCode (it is the username),
 * existingUserId (only for create), status (POST /staff/{id}/status), addresses (PUT /persons/{personId}/addresses),
 * photo (later PUT /persons/{personId}/photo, so this update can never wipe it).
 */
public record StaffUpdateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 80) String firstNameKh,
        @Size(max = 80) String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        @Pattern(regexp = "^[A-Z]{2}$", message = "{validation.country_code}") String nationalityCode,   // empty = KH
        @Size(max = 50) String nationalId,
        @Size(max = 30) String phone,
        @Email(regexp = Emails.REGEX, message = "{validation.email}") @Size(max = 150) String email,

        Long departmentId,
        @Size(max = 100) String position,
        @Size(max = 100) String positionKh,
        @Size(max = 50) String academicTitle,   // Dr., Prof., Assoc. Prof.
        @Size(max = 50) String academicTitleKh,
        @NotNull Staff.StaffType staffType,
        LocalDate hireDate) {
}
