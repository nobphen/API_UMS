package com.ums.finance.payment.api.dto;

import com.ums.finance.Currency;
import com.ums.finance.payment.domain.Payment;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        String paymentNo,
        Long invoiceId,
        BigDecimal amount,
        Currency currency,
        BigDecimal exchangeRate,
        Payment.Method method,
        String referenceNo,
        OffsetDateTime paidAt,
        Long receivedBy,
        Payment.Status status) {
}
