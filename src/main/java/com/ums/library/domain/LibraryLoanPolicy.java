package com.ums.library.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Borrowing rules for one kind of borrower: how many books, for how long, and the fines. */
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "library_loan_policies")
public class LibraryLoanPolicy extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "borrower_type", nullable = false, unique = true)
    private BorrowerType borrowerType;

    /** Books on loan at the same time; 0 = may not borrow. */
    @Column(name = "max_books", nullable = false)
    private int maxBooks;

    @Column(name = "loan_days", nullable = false)
    private int loanDays;

    @Column(name = "max_renewals", nullable = false)
    private int maxRenewals;

    /** Days added to the due date per renewal. */
    @Column(name = "renew_days", nullable = false)
    private int renewDays;

    @Column(name = "fine_per_day", nullable = false)
    private BigDecimal finePerDay = BigDecimal.ZERO;

    /** Cap on one loan's overdue fine; null = no cap. */
    @Column(name = "max_fine_per_loan")
    private BigDecimal maxFinePerLoan;

    /** Charged on top of the copy's price when a book is lost. */
    @Column(name = "lost_processing_fee", nullable = false)
    private BigDecimal lostProcessingFee = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    /** A READY reservation expires if not collected within this many days. */
    @Column(name = "reservation_hold_days", nullable = false)
    private int reservationHoldDays = 3;

    /** No new loans while the borrower has an UNPAID fine. */
    @Column(name = "block_on_unpaid_fines", nullable = false)
    private boolean blockOnUnpaidFines = true;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * UNDERGRADUATE: Foundation to Bachelor students. POSTGRADUATE: Master and PhD students.
     * LECTURER: staff type LECTURER or BOTH. STAFF: office staff.
     */
    public enum BorrowerType { UNDERGRADUATE, POSTGRADUATE, LECTURER, STAFF }
}
