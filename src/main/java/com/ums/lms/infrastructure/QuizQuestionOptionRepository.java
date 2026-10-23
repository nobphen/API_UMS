package com.ums.lms.infrastructure;

import com.ums.lms.domain.QuizQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionOptionRepository extends JpaRepository<QuizQuestionOption, Long> {
}
