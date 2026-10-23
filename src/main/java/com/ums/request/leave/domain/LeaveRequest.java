package com.ums.request.leave.domain;

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

/** A student asks to be excused from classes between two dates; each affected class's teacher decides. */
@Getter
@Setter
@Entity
@Table(name = "leave_requests")
public class LeaveRequest extends BaseEntity {

    @Column(name = "request_no", nullable = false, unique = true)
    private String requestNo;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    /** The chat thread about this request (messaging module). */
    @Column(name = "conversation_id", unique = true)
    private Long conversationId;

    /** PARTIAL: some teachers approved, others rejected. */
    public enum Status { PENDING, APPROVED, PARTIAL, REJECTED, CANCELLED }
    /** Filled by the database when the row is created. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
