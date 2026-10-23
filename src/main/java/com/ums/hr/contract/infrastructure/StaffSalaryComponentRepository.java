package com.ums.hr.contract.infrastructure;

import com.ums.hr.contract.domain.StaffSalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffSalaryComponentRepository extends JpaRepository<StaffSalaryComponent, Long> {
}
