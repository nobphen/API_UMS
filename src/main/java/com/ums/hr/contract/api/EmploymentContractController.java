package com.ums.hr.contract.api;

import com.ums.hr.contract.application.EmploymentContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employment-contracts")
@RequiredArgsConstructor
public class EmploymentContractController {

    private final EmploymentContractService employmentContractService;
}
