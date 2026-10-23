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
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** One physical copy of a book, identified by its barcode. */
@Getter
@Setter
@Entity
@Table(name = "book_copies")
public class BookCopy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "barcode", nullable = false, unique = true)
    private String barcode;

    @Column(name = "shelf_location")
    private String shelfLocation;

    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    /** What the library paid; charged to the borrower when the copy is lost. */
    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.AVAILABLE;

    public enum Status { AVAILABLE, ON_LOAN, RESERVED, LOST, DAMAGED, WITHDRAWN }
}
