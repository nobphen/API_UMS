package com.ums.library.api.dto;

import com.ums.library.domain.BookCopy;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookCopyResponse(
        Long id,
        Long bookId,
        String barcode,
        String shelfLocation,
        LocalDate acquiredDate,
        BigDecimal price,
        BookCopy.Status status) {
}
