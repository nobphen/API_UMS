package com.ums.engagement.survey.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "survey_questions")
public class SurveyQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "question_text_kh")
    private String questionTextKh;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    /** JSON list of choices for SINGLE and MULTIPLE questions. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options")
    private String options;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public enum QuestionType { RATING, TEXT, SINGLE, MULTIPLE }
}
