package com.ums.people.api.dto;

import com.ums.common.response.PhoneDisplay;

/**
 * One guardian of a student, with the link details (relationship, primary).
 * guardianId is for GET /guardians/{guardianId} (full details) and for PUT / DELETE of this link.
 */
public record StudentGuardianResponse(
        Long studentId,
        Long guardianId,
        String relationship,
        boolean primary,
        String firstName,
        String lastName,
        String firstNameKh,
        String lastNameKh,
        @PhoneDisplay String phone,   // the number to call
        String occupation) {
}
