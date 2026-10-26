package com.ums.finance.invoice.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.BaseEntity;
import com.ums.finance.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** A bill for either a student or an applicant: exactly one of the two is set. */
@Getter
@Setter
@Auditable
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @Column(name = "invoice_no", nullable = false, unique = true)
    private String invoiceNo;

    @Column(name = "student_id")
    private Long studentId;

    /** The applicant (admission module), for fees billed before they become a student. */
    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "semester_id")
    private Long semesterId;

    /** Sum of the item amounts. */
    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** subtotal minus discountAmount. */
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency = Currency.USD;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.UNPAID;

    public enum Status { UNPAID, PARTIAL, PAID, CANCELLED, OVERDUE }
    /** The user who issued the invoice. */
    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    /** Filled by the database when the row is created. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
