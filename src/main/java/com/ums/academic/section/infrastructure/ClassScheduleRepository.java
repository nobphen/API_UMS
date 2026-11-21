package com.ums.academic.section.infrastructure;

import com.ums.academic.section.domain.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for class schedules (table class_schedules: day, time and room of a class). */
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {

    /**
     * Whether a class is scheduled in this room. Not used yet (for the room "in use" check later).
     * class_schedules has no deleted_at: a removed schedule row is really deleted.
     * <pre>
     * SELECT c.id FROM class_schedules c WHERE c.room_id = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param roomId room id
     * @return true when at least one class uses the room
     */
    boolean existsByRoomId(Long roomId);
}
