package com.ums.hr.payroll.application.impl;

import com.ums.hr.payroll.application.PayrollPeriodService;
import com.ums.hr.payroll.infrastructure.PayrollPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PayrollPeriodServiceImpl implements PayrollPeriodService {

    private final PayrollPeriodRepository payrollPeriodRepository;
}
