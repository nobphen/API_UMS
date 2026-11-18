package com.ums.people.api.dto;

import com.ums.common.response.PhoneDisplay;
import com.ums.common.storage.FileUrl;
import com.ums.people.domain.Person;
import com.ums.people.domain.Staff;
import java.time.LocalDate;

/**
 * A staff member (person details + job details) sent back to the client. userId is null when there is no login.
 * personId is for the person endpoints: PUT /persons/{personId}/addresses and PUT /persons/{personId}/photo.
 */
public record StaffResponse(
        Long id,
        Long personId,
        String staffCode,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        @PhoneDisplay String phone,
        String email,
        @FileUrl String photoUrl,
        Long departmentId,
        String position,
        String positionKh,
        String academicTitle,
        String academicTitleKh,
        Staff.StaffType staffType,
        LocalDate hireDate,
        Staff.Status status,
        Long userId) {
}
