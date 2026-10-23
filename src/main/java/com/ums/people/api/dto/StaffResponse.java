package com.ums.people.api.dto;

import com.ums.people.domain.Person;
import com.ums.people.domain.Staff;
import java.time.LocalDate;

public record StaffResponse(
        Long id,
        String staffCode,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        Person.Gender gender,
        LocalDate dob,
        String phone,
        String email,
        Long departmentId,
        String position,
        String academicTitle,
        Staff.StaffType staffType,
        LocalDate hireDate,
        Staff.Status status,
        Long userId) {
}
