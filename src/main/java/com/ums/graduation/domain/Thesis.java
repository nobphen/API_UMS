package com.ums.graduation.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** A Bachelor thesis, Master's thesis or PhD dissertation: proposal → research → submission → defense. */
@Getter
@Setter
@Entity
@Table(name = "theses")
public class Thesis extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "thesis_type", nullable = false)
    private ThesisType thesisType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_kh")
    private String titleKh;

    /** The abstract. */
    @Column(name = "summary")
    private String summary;

    @Column(name = "summary_kh")
    private String summaryKh;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PROPOSAL;

    @Column(name = "proposal_approved_on")
    private LocalDate proposalApprovedOn;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "defense_at")
    private OffsetDateTime defenseAt;

    @Column(name = "defense_room_id")
    private Long defenseRoomId;

    @Column(name = "result_score")
    private BigDecimal resultScore;

    /** e.g. A, Excellent, Pass with minor revisions. */
    @Column(name = "result_grade")
    private String resultGrade;

    /** The final approved copy. */
    @Column(name = "file_url")
    private String fileUrl;

    /** Filled by the database when the proposal is first recorded. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public enum ThesisType { BACHELOR_THESIS, MASTER_THESIS, PHD_DISSERTATION, PROJECT }

    public enum Status {
        PROPOSAL, APPROVED, IN_PROGRESS, SUBMITTED, DEFENSE_SCHEDULED, PASSED, REVISION, FAILED, WITHDRAWN
    }
}
