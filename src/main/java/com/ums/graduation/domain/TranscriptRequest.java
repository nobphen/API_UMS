package com.ums.graduation.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "transcript_requests")
public class TranscriptRequest extends BaseEntity {

    @Column(name = "request_no", nullable = false, unique = true)
    private String requestNo;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transcript_type", nullable = false)
    private TranscriptType transcriptType;

    @Column(name = "copies", nullable = false)
    private int copies = 1;

    /** The fee invoice (finance module). */
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    public enum TranscriptType { OFFICIAL, UNOFFICIAL }

    public enum Status { PENDING, PAID, PRINTED, COLLECTED, CANCELLED }
}
