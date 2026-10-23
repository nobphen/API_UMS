package com.ums.hr.contract.infrastructure;

import com.ums.hr.contract.domain.SalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {
}
