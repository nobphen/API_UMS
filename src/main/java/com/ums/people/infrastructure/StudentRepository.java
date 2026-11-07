package com.ums.people.infrastructure;

import com.ums.people.domain.Student;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Database access for student records (table students). */
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

    /**
     * Whether a student studies (or studied) this program, deleted students not counted. Used by ProgramService.delete.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM students s WHERE s.program_id = ? AND s.deleted_at IS NULL)
     * </pre>
     *
     * @param programId program id
     * @return true when at least one student record uses the program
     */
    boolean existsByProgramIdAndDeletedAtIsNull(Long programId);

    /**
     * Whether the person has ANOTHER student record (not deleted). Used by StudentService.delete.
     * <pre>
     * SELECT s.id FROM students s WHERE s.person_id = ? AND s.id <> ? AND s.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param personId  person id
     * @param studentId the student record being deleted (not counted)
     * @return true when another student record uses the person
     */
    boolean existsByPerson_IdAndIdNotAndDeletedAtIsNull(Long personId, Long studentId);

    /**
     * Students for the list page, skipping deleted ones. Each filter that is null is not used.
     * q filters by student code or name, English or Khmer (empty q = all).
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=studentCode,asc).
     * The person is loaded in the same SELECT (join fetch), so reading the name needs no extra query.
     * <pre>
     * SELECT s.*, p.* FROM students s JOIN persons p ON p.id = s.person_id
     * WHERE s.deleted_at IS NULL
     *   AND (? IS NULL OR s.program_id = ?) AND (? IS NULL OR s.batch_id = ?)
     *   AND (? IS NULL OR s.year_level = ?) AND (? IS NULL OR s.status = ?)
     *   AND (LOWER(s.student_code) LIKE LOWER('%' || ? || '%') OR LOWER(p.first_name) LIKE ...
     *        OR LOWER(p.last_name) LIKE ... OR p.first_name_kh LIKE ... OR p.last_name_kh LIKE ...)
     * ORDER BY ... LIMIT ? OFFSET ?
     * </pre>
     * countQuery: the same WHERE with count(s), for the page totals (Spring can't build it by itself with join fetch).
     *
     * @param q         search text; "" = all
     * @param programId program id, or null for all programs
     * @param batchId   batch id, or null for all batches
     * @param yearLevel year of study, or null for all years
     * @param status    student status, or null for all
     * @param pageable  page number, size and sort
     * @return one page of students, with the totals
     */
    @Query(value = """
            select s from Student s
            join fetch s.person p
            where s.deletedAt is null
              and (:programId is null or s.programId = :programId)
              and (:batchId is null or s.batchId = :batchId)
              and (:yearLevel is null or s.yearLevel = :yearLevel)
              and (:status is null or s.status = :status)
              and (lower(s.studentCode) like lower(concat('%', :q, '%'))
                   or lower(p.firstName) like lower(concat('%', :q, '%'))
                   or lower(p.lastName) like lower(concat('%', :q, '%'))
                   or p.firstNameKh like concat('%', :q, '%')
                   or p.lastNameKh like concat('%', :q, '%'))
            """,
           countQuery = """
            select count(s) from Student s
            join s.person p
            where s.deletedAt is null
              and (:programId is null or s.programId = :programId)
              and (:batchId is null or s.batchId = :batchId)
              and (:yearLevel is null or s.yearLevel = :yearLevel)
              and (:status is null or s.status = :status)
              and (lower(s.studentCode) like lower(concat('%', :q, '%'))
                   or lower(p.firstName) like lower(concat('%', :q, '%'))
                   or lower(p.lastName) like lower(concat('%', :q, '%'))
                   or p.firstNameKh like concat('%', :q, '%')
                   or p.lastNameKh like concat('%', :q, '%'))
            """)
    Page<Student> search(String q, Long programId, Long batchId, Integer yearLevel,
                         Student.Status status, Pageable pageable);
}
