package com.ums.exam.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A student's seat in one exam, and whether they may sit it. */
@Getter
@Setter
@Entity
@Table(name = "exam_seat_assignments")
public class ExamSeatAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_schedule_id")
    private ExamSchedule examSchedule;

    /** The student's enrollment in the class section (enrollment module). */
    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "seat_no")
    private String seatNo;

    @Column(name = "is_eligible", nullable = false)
    private boolean eligible = true;

    /** Why the student may not sit the exam, e.g. too many absences or unpaid fees. */
    @Column(name = "ineligible_reason")
    private String ineligibleReason;

    /** Null until the exam has taken place. */
    @Column(name = "attended")
    private Boolean attended;
}
