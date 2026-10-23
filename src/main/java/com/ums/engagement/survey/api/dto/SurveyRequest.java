package com.ums.engagement.survey.api.dto;

import com.ums.engagement.survey.domain.Survey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record SurveyRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 200) String titleKh,
        @NotNull Survey.SurveyType surveyType,
        Long semesterId,
        Boolean anonymous,   // empty = true
        OffsetDateTime openAt,
        OffsetDateTime closeAt) {
}
