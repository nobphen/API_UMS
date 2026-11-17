package com.ums.people.infrastructure;

import com.ums.people.domain.StudentGuardian;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for student-guardian links (table student_guardians). */
public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, StudentGuardian.Id> {

    /**
     * Removes every student link of one guardian. Used by GuardianService.delete:
     * a soft delete does not fire ON DELETE CASCADE, so the links are removed here.
     * One DELETE with @Query (a derived name like deleteById_GuardianId would SELECT every row, then DELETE one by one).
     * The key is inside the @EmbeddedId, so the path is sg.id.guardianId.
     * <pre>
     * DELETE FROM student_guardians WHERE guardian_id = ?
     * </pre>
     *
     * @param guardianId guardian id
     * @return how many links were removed
     */
    @Modifying
    @Query("delete from StudentGuardian sg where sg.id.guardianId = :guardianId")
    int deleteByGuardianId(@Param("guardianId") Long guardianId);

    /**
     * Whether the student has any guardian yet. Used by link: the first guardian is always primary.
     * The key is inside the @EmbeddedId, so the name says Id_StudentId (= sg.id.studentId).
     * <pre>
     * SELECT 1 FROM student_guardians WHERE student_id = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param studentId student id
     * @return true when at least one guardian is linked
     */
    boolean existsById_StudentId(Long studentId);

    /**
     * The student's links, primary first. Used by list, and by unlink to choose a new primary.
     * <pre>
     * SELECT * FROM student_guardians WHERE student_id = ? ORDER BY is_primary DESC
     * </pre>
     *
     * @param studentId student id
     * @return the links; empty when none
     */
    List<StudentGuardian> findById_StudentIdOrderByPrimaryDesc(Long studentId);

    /**
     * Makes every OTHER guardian of the student not primary, so only one stays primary.
     * Used by link and update before this guardian becomes primary.
     * The guardian itself is left out on purpose: in update it is already loaded, and changing its row
     * here would leave the loaded copy out of date (JPA would then skip its UPDATE).
     * <pre>
     * UPDATE student_guardians SET is_primary = false WHERE student_id = ? AND guardian_id <> ?
     * </pre>
     *
     * @param studentId  student id
     * @param guardianId the guardian who becomes primary (not changed)
     * @return how many links were changed (0 or 1)
     */
    @Modifying
    @Query("update StudentGuardian sg set sg.primary = false"
            + " where sg.id.studentId = :studentId and sg.id.guardianId <> :guardianId")
    int clearPrimaryExcept(@Param("studentId") Long studentId, @Param("guardianId") Long guardianId);
}
