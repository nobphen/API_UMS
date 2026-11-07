package com.ums.people.infrastructure;

import com.ums.people.domain.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for guardians (table guardians). */
public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    /**
     * Whether the person is a guardian (not deleted). Used by StudentService.delete.
     * <pre>
     * SELECT g.id FROM guardians g WHERE g.person_id = ? AND g.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param personId person id
     * @return true when the person is a guardian
     */
    boolean existsByPerson_IdAndDeletedAtIsNull(Long personId);
}
