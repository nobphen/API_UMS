package com.ums.hr.contract.application.impl;

import com.ums.hr.contract.application.SalaryComponentService;
import com.ums.hr.contract.infrastructure.SalaryComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SalaryComponentServiceImpl implements SalaryComponentService {

    private final SalaryComponentRepository salaryComponentRepository;
}
