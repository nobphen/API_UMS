package com.ums.finance.fee.infrastructure;

import com.ums.finance.fee.domain.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeTypeRepository extends JpaRepository<FeeType, Long> {
}
