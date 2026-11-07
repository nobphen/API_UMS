package com.ums.people.infrastructure;

import com.ums.people.domain.Staff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for staff (table staff). */
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * The staff record of a login, skipping deleted ones.
     * <pre>
     * SELECT s.* FROM staff s WHERE s.user_id = ? AND s.deleted_at IS NULL
     * </pre>
     *
     * @param userId id of the login (users.id)
     * @return the staff record, or empty when the login is not staff
     */
    Optional<Staff> findByUserIdAndDeletedAtIsNull(Long userId);

    /**
     * Whether the person has a staff record (not deleted). Used by StudentService.delete.
     * <pre>
     * SELECT s.id FROM staff s WHERE s.person_id = ? AND s.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param personId person id
     * @return true when the person is staff
     */
    boolean existsByPerson_IdAndDeletedAtIsNull(Long personId);
}
