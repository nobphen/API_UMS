package com.ums.finance.invoice.api.dto;

import com.ums.finance.Currency;
import com.ums.finance.invoice.domain.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record InvoiceResponse(
        Long id,
        String invoiceNo,
        Long studentId,
        Long applicantId,
        Long semesterId,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        Currency currency,
        LocalDate dueDate,
        Invoice.Status status,
        Long createdBy,
        OffsetDateTime createdAt) {
}
