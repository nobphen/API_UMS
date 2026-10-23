package com.ums.hr.payroll.api;

import com.ums.hr.payroll.application.PayrollPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payroll-periods")
@RequiredArgsConstructor
public class PayrollPeriodController {

    private final PayrollPeriodService payrollPeriodService;
}
