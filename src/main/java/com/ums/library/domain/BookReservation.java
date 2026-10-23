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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** A borrower waiting for any copy of a book. */
@Getter
@Setter
@Entity
@Table(name = "book_reservations")
public class BookReservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    /** The borrower's login account. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reserved_at", nullable = false)
    private OffsetDateTime reservedAt = OffsetDateTime.now();

    /** Set when the reservation becomes READY: it expires if not collected by then. */
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.WAITING;

    public enum Status { WAITING, READY, FULFILLED, CANCELLED, EXPIRED }
}
