package com.ums.lms.api.dto;

import com.ums.lms.domain.QuizQuestion;
import java.math.BigDecimal;

public record QuizQuestionResponse(
        Long id,
        Long assessmentId,
        String questionText,
        QuizQuestion.QuestionType questionType,
        BigDecimal points,
        int sortOrder) {
}
