package com.ums.people.api.dto;

import com.ums.people.domain.Person;
import com.ums.people.domain.Staff;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * HR adds a staff member; a login account is made with the staff code as username.
 * For someone who already has a login (e.g. a graduate hired as a lecturer), pass existingUserId: no new login is made,
 * and their stored personal details are used.
 */
public record StaffRequest(
        Long existingUserId,

        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "may only contain letters, digits, '.', '_' and '-'")
        String staffCode,   // also the username

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

        Long departmentId,
        @Size(max = 100) String position,
        @Size(max = 100) String positionKh,
        @Size(max = 50) String academicTitle,   // Dr., Prof., Assoc. Prof.
        @Size(max = 50) String academicTitleKh,
        @NotNull Staff.StaffType staffType,
        LocalDate hireDate) {
}
