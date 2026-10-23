package com.ums.library.api.dto;

import com.ums.library.domain.BookCopy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookCopyRequest(
        @NotNull Long bookId,
        @NotBlank @Size(max = 50) String barcode,
        @Size(max = 50) String shelfLocation,
        LocalDate acquiredDate,
        @PositiveOrZero BigDecimal price,
        BookCopy.Status status) {   // empty = AVAILABLE; the librarian marks a copy DAMAGED, LOST or WITHDRAWN
}
