package com.ums.request.studentrequest.domain;

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

/** One approval step of a student request. */
@Getter
@Setter
@Entity
@Table(name = "request_approvals")
public class RequestApproval extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_request_id")
    private StudentRequest studentRequest;

    /** Order of the step, starting at 1. */
    @Column(name = "step_no", nullable = false)
    private int stepNo;

    /** Anyone with this role may decide the step. */
    @Column(name = "approver_role_id", nullable = false)
    private Long approverRoleId;

    /** The user who decided the step. */
    @Column(name = "approver_id")
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "comment")
    private String comment;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    public enum Status { PENDING, APPROVED, REJECTED, SKIPPED }
}
