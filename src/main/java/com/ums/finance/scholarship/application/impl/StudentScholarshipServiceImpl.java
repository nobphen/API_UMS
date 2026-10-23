package com.ums.finance.scholarship.application.impl;

import com.ums.finance.scholarship.application.StudentScholarshipService;
import com.ums.finance.scholarship.infrastructure.StudentScholarshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentScholarshipServiceImpl implements StudentScholarshipService {

    private final StudentScholarshipRepository studentScholarshipRepository;
}
