package com.ums.library.api.dto;

import com.ums.library.domain.BookReservation;
import java.time.OffsetDateTime;

public record BookReservationResponse(
        Long id,
        Long bookId,
        Long userId,
        OffsetDateTime reservedAt,
        OffsetDateTime expiresAt,
        BookReservation.Status status) {
}
