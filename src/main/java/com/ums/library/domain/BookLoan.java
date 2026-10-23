package com.ums.library.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** A copy lent to a borrower. A copy has at most one loan not yet returned. */
@Getter
@Setter
@Entity
@Table(name = "book_loans")
public class BookLoan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "copy_id")
    private BookCopy copy;

    /** The borrower's login account. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The librarian who lent the book. */
    @Column(name = "issued_by")
    private Long issuedBy;

    @Column(name = "borrowed_at", nullable = false)
    private OffsetDateTime borrowedAt = OffsetDateTime.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    @Column(name = "renew_count", nullable = false)
    private int renewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ON_LOAN;

    public enum Status { ON_LOAN, RETURNED, OVERDUE, LOST }
}
