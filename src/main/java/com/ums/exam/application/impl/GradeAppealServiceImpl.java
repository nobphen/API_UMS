package com.ums.exam.application.impl;

import com.ums.exam.application.GradeAppealService;
import com.ums.exam.infrastructure.GradeAppealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GradeAppealServiceImpl implements GradeAppealService {

    private final GradeAppealRepository gradeAppealRepository;
}
