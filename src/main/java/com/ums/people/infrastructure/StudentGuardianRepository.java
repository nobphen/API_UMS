package com.ums.people.infrastructure;

import com.ums.people.domain.StudentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for student-guardian links (table student_guardians). Only the basic JpaRepository methods for now. */
public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, StudentGuardian.Id> {
}
