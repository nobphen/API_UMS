package com.ums.engagement.survey.application.impl;

import com.ums.engagement.survey.application.SurveyService;
import com.ums.engagement.survey.infrastructure.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
}
