package com.ums.engagement.survey.infrastructure;

import com.ums.engagement.survey.domain.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
}
