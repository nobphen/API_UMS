package com.ums.lms.api.dto;

import com.ums.lms.domain.QuizQuestion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record QuizQuestionRequest(
        @NotNull Long assessmentId,
        @NotBlank String questionText,
        @NotNull QuizQuestion.QuestionType questionType,
        @PositiveOrZero BigDecimal points,   // empty = 1
        @PositiveOrZero Integer sortOrder) {   // empty = 0
}
