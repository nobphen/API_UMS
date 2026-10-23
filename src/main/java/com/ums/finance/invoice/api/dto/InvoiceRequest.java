package com.ums.finance.invoice.api.dto;

import com.ums.finance.Currency;
import java.time.LocalDate;

/**
 * An invoice for exactly one of studentId or applicantId. The invoice number, totals and status are set
 * by the server.
 */
public record InvoiceRequest(
        Long studentId,
        Long applicantId,
        Long semesterId,
        Currency currency,   // empty = USD
        LocalDate dueDate) {
}
