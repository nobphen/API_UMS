package com.ums.graduation.infrastructure;

import com.ums.graduation.domain.GraduationApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationApplicationRepository extends JpaRepository<GraduationApplication, Long> {
}
