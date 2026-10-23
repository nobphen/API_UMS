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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** One class section affected by a leave request, decided by that class's teacher. */
@Getter
@Setter
@Entity
@Table(name = "leave_request_classes")
public class LeaveRequestClass extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_request_id")
    private LeaveRequest leaveRequest;

    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    /** The staff member who teaches the class and decides. */
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "comment")
    private String comment;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    public enum Status { PENDING, APPROVED, REJECTED }
}
