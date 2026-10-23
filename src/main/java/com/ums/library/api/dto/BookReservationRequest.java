package com.ums.library.api.dto;

import jakarta.validation.constraints.NotNull;

public record BookReservationRequest(
        @NotNull Long bookId,
        @NotNull Long userId) {   // the borrower's login account
}
