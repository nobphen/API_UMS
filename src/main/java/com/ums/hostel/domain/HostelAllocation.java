package com.ums.hostel.domain;

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
import lombok.Getter;
import lombok.Setter;

/** A bed given to a student for one semester. A bed, and a student, have at most one ACTIVE allocation. */
@Getter
@Setter
@Entity
@Table(name = "hostel_allocations")
public class HostelAllocation extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bed_id")
    private HostelBed bed;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    /** The finance invoice for the hostel fee. */
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.RESERVED;

    public enum Status { RESERVED, ACTIVE, CHECKED_OUT, CANCELLED }
}
