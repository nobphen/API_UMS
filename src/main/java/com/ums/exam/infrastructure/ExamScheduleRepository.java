package com.ums.exam.infrastructure;

import com.ums.exam.domain.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for exam schedules (table exam_schedules: when and in which room an exam is). */
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    /**
     * Whether an exam (not deleted) is planned in this room. Not used yet (for the room "in use" check later).
     * <pre>
     * SELECT e.id FROM exam_schedules e WHERE e.room_id = ? AND e.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param roomId room id
     * @return true when at least one exam uses the room
     */
    boolean existsByRoomIdAndDeletedAtIsNull(Long roomId);
}
