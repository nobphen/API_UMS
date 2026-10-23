package com.ums.library.api.dto;

import jakarta.validation.constraints.NotNull;

/** The librarian lends a copy; the due date comes from the borrower's loan policy. */
public record BookLoanRequest(
        @NotNull Long copyId,
        @NotNull Long userId) {   // the borrower's login account
}
