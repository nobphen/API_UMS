package com.ums.lms.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** A student's work handed in for an assignment (an assessment); each resubmission is a new attempt. */
@Getter
@Setter
@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission extends BaseEntity {

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "text_answer")
    private String textAnswer;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt = OffsetDateTime.now();

    /** Handed in after the due date. */
    @Column(name = "is_late", nullable = false)
    private boolean late = false;

    @Column(name = "attempt_no", nullable = false)
    private int attemptNo = 1;

    /** The lecturer's comments. */
    @Column(name = "feedback")
    private String feedback;
}
