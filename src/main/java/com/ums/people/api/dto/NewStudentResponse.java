package com.ums.people.api.dto;

/** Returned only when the student is created: print the login slip now, the temporary password is not stored. */
public record NewStudentResponse(StudentResponse student, String username, String temporaryPassword) {
}
