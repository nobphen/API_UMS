package com.ums.finance.scholarship.application.impl;

import com.ums.finance.scholarship.application.ScholarshipService;
import com.ums.finance.scholarship.infrastructure.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScholarshipServiceImpl implements ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;
}
