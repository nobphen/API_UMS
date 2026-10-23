package com.ums.engagement.survey.infrastructure;

import com.ums.engagement.survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
}
