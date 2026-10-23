package com.ums.people.infrastructure;

import com.ums.people.domain.Student;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentCodeIgnoreCase(String studentCode);

    /** Every program the login has studied or is studying, newest first. */
    List<Student> findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(Long userId);

    /** True when the login is already a current student of this program. */
    boolean existsByUserIdAndProgramIdAndStatusInAndDeletedAtIsNull(Long userId, Long programId,
                                                                     Collection<Student.Status> statuses);
}
