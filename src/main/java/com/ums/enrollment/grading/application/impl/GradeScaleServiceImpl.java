package com.ums.enrollment.grading.application.impl;

import com.ums.enrollment.grading.application.GradeScaleService;
import com.ums.enrollment.grading.infrastructure.GradeScaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GradeScaleServiceImpl implements GradeScaleService {

    private final GradeScaleRepository gradeScaleRepository;
}
