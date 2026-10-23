package com.ums.engagement.survey.infrastructure;

import com.ums.engagement.survey.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
}
