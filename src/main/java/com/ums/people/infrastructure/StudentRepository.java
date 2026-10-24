package com.ums.people.infrastructure;

import com.ums.people.domain.Student;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Whether a student already uses this student code, ignoring case.
     * <pre>
     * SELECT s.id FROM students s WHERE upper(s.student_code) = upper(?) FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param studentCode student code to check
     * @return true when the code is taken
     */
    boolean existsByStudentCodeIgnoreCase(String studentCode);

    /**
     * Every program the login has studied or is studying, newest first.
     * <pre>
     * SELECT s.* FROM students s
     * WHERE s.user_id = ? AND s.deleted_at IS NULL
     * ORDER BY s.admission_date DESC
     * </pre>
     *
     * @param userId id of the login (users.id)
     * @return the login's student records, newest admission first
     */
    List<Student> findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(Long userId);

    /**
     * The students of one program and year, for moving them up a year.
     * <pre>
     * SELECT s.* FROM students s
     * WHERE s.program_id = ? AND s.year_level = ? AND s.status = ? AND s.deleted_at IS NULL
     * </pre>
     *
     * @param programId id of the program
     * @param yearLevel year of study, e.g. 1 for first year
     * @param status    student status to match, e.g. ACTIVE
     * @return the matching students
     */
    List<Student> findByProgramIdAndYearLevelAndStatusAndDeletedAtIsNull(Long programId, int yearLevel, Student.Status status);

    /**
     * True when the login is already a current student of this program.
     * <pre>
     * SELECT s.id FROM students s
     * WHERE s.user_id = ? AND s.program_id = ? AND s.status IN (?, ?, ...) AND s.deleted_at IS NULL
     * FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param userId    id of the login (users.id)
     * @param programId id of the program
     * @param statuses  statuses that count as current
     * @return true when such a student record exists
     */
    boolean existsByUserIdAndProgramIdAndStatusInAndDeletedAtIsNull(Long userId, Long programId,
                                                                     Collection<Student.Status> statuses);
}
