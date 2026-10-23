package com.ums.engagement.survey.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "surveys")
public class Survey extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_kh")
    private String titleKh;

    @Enumerated(EnumType.STRING)
    @Column(name = "survey_type", nullable = false)
    private SurveyType surveyType;

    /** The semester being evaluated (academic module). */
    @Column(name = "semester_id")
    private Long semesterId;

    /** Responses are not linked to the respondent. */
    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous = true;

    @Column(name = "open_at")
    private OffsetDateTime openAt;

    @Column(name = "close_at")
    private OffsetDateTime closeAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;

    public enum SurveyType { TEACHER_EVALUATION, COURSE_FEEDBACK, GENERAL, ALUMNI }

    public enum Status { DRAFT, OPEN, CLOSED }
    /** The user who made the survey. */
    @Column(name = "created_by")
    private Long createdBy;
}
