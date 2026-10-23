package com.ums.library.api.dto;

import com.ums.library.domain.LibraryFine;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** The librarian fines a borrower by hand, e.g. for a damaged book; the borrower is the loan's borrower. */
public record LibraryFineRequest(
        @NotNull Long loanId,
        @NotNull LibraryFine.Reason reason,
        @NotNull @PositiveOrZero BigDecimal amount) {
}
