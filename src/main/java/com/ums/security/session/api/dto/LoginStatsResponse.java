package com.ums.security.session.api.dto;

import java.time.LocalDate;

/** How many logins and logouts happened on one day (Cambodia time). */
public record LoginStatsResponse(
        LocalDate date,
        long logins,     // new sessions: password, QR or SMS code
        long logouts) {  // sessions the user ended with "Log out"
}
