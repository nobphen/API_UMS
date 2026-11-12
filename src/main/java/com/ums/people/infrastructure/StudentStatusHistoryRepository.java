package com.ums.people.infrastructure;

import com.ums.people.domain.StudentStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for student status changes (table student_status_history). */
public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {

    /**
     * Every status change of one student record, newest first. Used by StudentService.getStatusHistory.
     * <pre>
     * SELECT h.* FROM student_status_history h WHERE h.student_id = ? ORDER BY h.changed_at DESC, h.id DESC
     * </pre>
     *
     * @param studentId student id
     * @return the changes, newest first (empty when the status never changed)
     */
    List<StudentStatusHistory> findByStudentIdOrderByChangedAtDescIdDesc(Long studentId);
}
