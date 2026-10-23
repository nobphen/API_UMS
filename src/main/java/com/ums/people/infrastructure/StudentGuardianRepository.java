package com.ums.people.infrastructure;

import com.ums.people.domain.StudentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, StudentGuardian.Id> {
}
