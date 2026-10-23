package com.ums.engagement.survey.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One submission of a survey (for one class section when the survey is per class). */
@Getter
@Setter
@Entity
@Table(name = "survey_responses")
public class SurveyResponse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    /** The class section being evaluated (academic module). */
    @Column(name = "class_section_id")
    private Long classSectionId;

    /** The login account (security module); null when the survey is anonymous. */
    @Column(name = "respondent_id")
    private Long respondentId;

    /** One-way hash of the respondent; blocks a second submission, anonymous surveys included. */
    @Column(name = "respondent_token", nullable = false)
    private String respondentToken;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", nullable = false)
    private String answers;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt = OffsetDateTime.now();
}
