package com.ums.hr.payroll.api;

import com.ums.hr.payroll.application.PayslipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;
}
