package com.ums.enrollment.grading.application.impl;

import com.ums.enrollment.grading.application.AssessmentService;
import com.ums.enrollment.grading.infrastructure.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
}
