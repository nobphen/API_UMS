package com.ums.finance.fee.infrastructure;

import com.ums.finance.fee.domain.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
}
