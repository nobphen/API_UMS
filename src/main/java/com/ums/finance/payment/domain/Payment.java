package com.ums.finance.payment.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.BaseEntity;
import com.ums.finance.Currency;
import com.ums.finance.invoice.domain.Invoice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Auditable
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(name = "payment_no", nullable = false, unique = true)
    private String paymentNo;

    /** Empty for walk-in application form sales. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency = Currency.USD;

    /** Used when the payment currency differs from the invoice currency. */
    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private Method method;

    /** Bank or payment gateway transaction reference. */
    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "paid_at", nullable = false)
    private OffsetDateTime paidAt = OffsetDateTime.now();

    /** The login account of the cashier who took the payment. */
    @Column(name = "received_by")
    private Long receivedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.SUCCESS;

    public enum Method { CASH, BANK_TRANSFER, ABA, KHQR, WING, ACLEDA }

    public enum Status { SUCCESS, REVERSED }
}
