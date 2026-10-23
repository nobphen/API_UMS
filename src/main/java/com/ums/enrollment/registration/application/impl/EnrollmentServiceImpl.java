package com.ums.enrollment.registration.application.impl;

import com.ums.enrollment.registration.application.EnrollmentService;
import com.ums.enrollment.registration.infrastructure.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
}
