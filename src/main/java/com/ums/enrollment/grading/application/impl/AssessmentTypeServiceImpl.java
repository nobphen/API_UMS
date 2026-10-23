package com.ums.enrollment.grading.application.impl;

import com.ums.enrollment.grading.application.AssessmentTypeService;
import com.ums.enrollment.grading.infrastructure.AssessmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AssessmentTypeServiceImpl implements AssessmentTypeService {

    private final AssessmentTypeRepository assessmentTypeRepository;
}
