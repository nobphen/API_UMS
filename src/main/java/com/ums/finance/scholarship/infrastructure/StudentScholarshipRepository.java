package com.ums.finance.scholarship.infrastructure;

import com.ums.finance.scholarship.domain.StudentScholarship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentScholarshipRepository extends JpaRepository<StudentScholarship, Long> {
}
