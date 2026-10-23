package com.ums.hr.payroll.application.impl;

import com.ums.hr.payroll.application.PayslipService;
import com.ums.hr.payroll.infrastructure.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PayslipServiceImpl implements PayslipService {

    private final PayslipRepository payslipRepository;
}
