package com.ums.hr.contract.application.impl;

import com.ums.hr.contract.application.EmploymentContractService;
import com.ums.hr.contract.infrastructure.EmploymentContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EmploymentContractServiceImpl implements EmploymentContractService {

    private final EmploymentContractRepository employmentContractRepository;
}
