package com.ums.people.api.dto;

/**
 * Returned only when the staff member is created: give them the login slip now, the temporary password is not stored.
 * temporaryPassword is null when an existing login was used (existingUserId): they keep their own password.
 */
public record NewStaffResponse(StaffResponse staff, String username, String temporaryPassword) {
}
