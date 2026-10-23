package com.ums.enrollment.registration.infrastructure;

import com.ums.enrollment.registration.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
