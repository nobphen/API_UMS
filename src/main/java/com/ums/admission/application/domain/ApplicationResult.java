package com.ums.admission.application.domain;

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
import lombok.Getter;
import lombok.Setter;

/** Exam and interview scores and the admission decision; one per application. */
@Getter
@Setter
@Entity
@Table(name = "application_results")
public class ApplicationResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", unique = true)
    private Application application;

    @Column(name = "exam_score")
    private BigDecimal examScore;

    @Column(name = "interview_score")
    private BigDecimal interviewScore;

    @Column(name = "total_score")
    private BigDecimal totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private Decision decision;

    /** A scholarship offered with the admission (finance module). */
    @Column(name = "scholarship_id")
    private Long scholarshipId;

    @Column(name = "remark")
    private String remark;

    public enum Decision { ACCEPTED, WAITLISTED, REJECTED }
}
