package com.ums.hr.payroll.infrastructure;

import com.ums.hr.payroll.domain.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
}
