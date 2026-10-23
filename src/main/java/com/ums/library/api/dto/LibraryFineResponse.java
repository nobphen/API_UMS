package com.ums.library.api.dto;

import com.ums.library.domain.LibraryFine;
import java.math.BigDecimal;

public record LibraryFineResponse(
        Long id,
        Long loanId,
        Long userId,
        LibraryFine.Reason reason,
        BigDecimal amount,
        Long invoiceId,
        LibraryFine.Status status,
        Long waivedBy) {
}
