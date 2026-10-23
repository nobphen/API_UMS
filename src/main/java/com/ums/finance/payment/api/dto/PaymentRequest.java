package com.ums.finance.payment.api.dto;

import com.ums.finance.Currency;
import com.ums.finance.payment.domain.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** A payment taken by a cashier. The payment number and time are set by the server. */
public record PaymentRequest(
        Long invoiceId,   // empty for a walk-in form sale
        @NotNull @Positive BigDecimal amount,
        Currency currency,   // empty = USD
        @Positive BigDecimal exchangeRate,   // needed when the currency differs from the invoice's
        @NotNull Payment.Method method,
        @Size(max = 100) String referenceNo) {
}
