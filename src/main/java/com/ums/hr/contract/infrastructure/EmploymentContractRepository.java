package com.ums.hr.contract.infrastructure;

import com.ums.hr.contract.domain.EmploymentContract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentContractRepository extends JpaRepository<EmploymentContract, Long> {
}
