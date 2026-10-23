package com.ums.enrollment.grading.application.impl;

import com.ums.enrollment.grading.application.FinalGradeService;
import com.ums.enrollment.grading.infrastructure.FinalGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FinalGradeServiceImpl implements FinalGradeService {

    private final FinalGradeRepository finalGradeRepository;
}
