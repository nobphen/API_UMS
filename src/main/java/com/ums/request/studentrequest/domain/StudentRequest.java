package com.ums.request.studentrequest.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A student's administrative request, approved step by step (see RequestApproval). */
@Getter
@Setter
@Entity
@Table(name = "student_requests")
public class StudentRequest extends BaseEntity {

    @Column(name = "request_no", nullable = false, unique = true)
    private String requestNo;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    /** Type-specific details as JSON, e.g. the sections to add or drop. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private String payload;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    /** The chat thread about this request (messaging module). */
    @Column(name = "conversation_id", unique = true)
    private Long conversationId;

    public enum RequestType { ADD_DROP, PROGRAM_TRANSFER, LEAVE_OF_ABSENCE, WITHDRAWAL, RE_EXAM, DOCUMENT }

    public enum Status { PENDING, IN_REVIEW, APPROVED, REJECTED, CANCELLED }
    /** Filled by the database when the row is created. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
