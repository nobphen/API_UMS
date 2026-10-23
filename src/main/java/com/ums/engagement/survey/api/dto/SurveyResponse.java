package com.ums.engagement.survey.api.dto;

import com.ums.engagement.survey.domain.Survey;
import java.time.OffsetDateTime;

public record SurveyResponse(
        Long id,
        String title,
        String titleKh,
        Survey.SurveyType surveyType,
        Long semesterId,
        boolean anonymous,
        OffsetDateTime openAt,
        OffsetDateTime closeAt,
        Survey.Status status,
        Long createdBy) {
}
