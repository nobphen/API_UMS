package com.ums.hr.payroll.infrastructure;

import com.ums.hr.payroll.domain.PayslipItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayslipItemRepository extends JpaRepository<PayslipItem, Long> {
}
