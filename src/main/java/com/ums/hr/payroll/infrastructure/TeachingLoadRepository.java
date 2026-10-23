package com.ums.hr.payroll.infrastructure;

import com.ums.hr.payroll.domain.TeachingLoad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingLoadRepository extends JpaRepository<TeachingLoad, Long> {
}
