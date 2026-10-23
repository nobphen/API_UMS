package com.ums.hr.payroll.application.impl;

import com.ums.hr.payroll.application.TeachingLoadService;
import com.ums.hr.payroll.infrastructure.TeachingLoadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeachingLoadServiceImpl implements TeachingLoadService {

    private final TeachingLoadRepository teachingLoadRepository;
}
