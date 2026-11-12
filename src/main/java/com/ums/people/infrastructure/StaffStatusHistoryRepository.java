package com.ums.people.infrastructure;

import com.ums.people.domain.StaffStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for staff status changes (table staff_status_history). */
public interface StaffStatusHistoryRepository extends JpaRepository<StaffStatusHistory, Long> {

    /**
     * Every status change of one staff member, newest first. Used by StaffService.getStatusHistory.
     * <pre>
     * SELECT h.* FROM staff_status_history h WHERE h.staff_id = ? ORDER BY h.changed_at DESC, h.id DESC
     * </pre>
     *
     * @param staffId staff id
     * @return the changes, newest first (empty when the status never changed)
     */
    List<StaffStatusHistory> findByStaffIdOrderByChangedAtDescIdDesc(Long staffId);
}
