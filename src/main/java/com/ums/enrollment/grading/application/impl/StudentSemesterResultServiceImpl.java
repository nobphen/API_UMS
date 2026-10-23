package com.ums.enrollment.grading.application.impl;

import com.ums.enrollment.grading.application.StudentSemesterResultService;
import com.ums.enrollment.grading.infrastructure.StudentSemesterResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentSemesterResultServiceImpl implements StudentSemesterResultService {

    private final StudentSemesterResultRepository studentSemesterResultRepository;
}
