package com.ums.hr.payroll.infrastructure;

import com.ums.hr.payroll.domain.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {
}
