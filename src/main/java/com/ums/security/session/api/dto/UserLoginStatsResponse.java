package com.ums.security.session.api.dto;

/** How many times one user logged in and out on one day (Cambodia time). */
public record UserLoginStatsResponse(
        Long userId,
        String username,
        String userType,   // STUDENT, STAFF, GUARDIAN, APPLICANT or ADMIN
        long logins,       // new sessions: password, QR or SMS code
        long logouts) {    // sessions the user ended with "Log out"
}
