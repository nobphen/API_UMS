package com.ums.library.api.dto;

import com.ums.library.domain.BookLoan;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record BookLoanResponse(
        Long id,
        Long copyId,
        Long userId,
        Long issuedBy,
        OffsetDateTime borrowedAt,
        LocalDate dueDate,
        OffsetDateTime returnedAt,
        int renewCount,
        BookLoan.Status status) {
}
