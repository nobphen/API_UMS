package com.ums.lms.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** A question of an online quiz (an assessment). */
@Getter
@Setter
@Entity
@Table(name = "quiz_questions")
public class QuizQuestion extends BaseEntity {

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    /** SINGLE: one correct option. MULTIPLE: several. SHORT: a typed answer, graded by hand. */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "points", nullable = false)
    private BigDecimal points = BigDecimal.ONE;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public enum QuestionType { SINGLE, MULTIPLE, TRUE_FALSE, SHORT }
}
