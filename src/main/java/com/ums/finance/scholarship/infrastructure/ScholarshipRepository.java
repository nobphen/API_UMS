package com.ums.finance.scholarship.infrastructure;

import com.ums.finance.scholarship.domain.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {
}
