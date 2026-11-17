package com.ums.people.infrastructure;

import com.ums.people.domain.Guardian;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Database access for guardians (table guardians).
 */
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

    /**
     * One guardian that is not deleted. Used by GuardianService get, update and delete (empty = 404).
     * <pre>
     * SELECT g.* FROM guardians g WHERE g.id = ? AND g.deleted_at IS NULL
     * </pre>
     *
     * @param id guardian id
     * @return the guardian, or empty when it does not exist or is deleted
     */
    Optional<Guardian> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Guardians for the list page, skipping deleted ones.
     * q filters by name, English or Khmer, or phone (empty q = all).
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=person.lastName,asc).
     * The person is loaded in the same SELECT (join fetch), so reading the name needs no extra query.
     * <pre>
     * SELECT g.*, p.* FROM guardians g JOIN persons p ON p.id = g.person_id
     * WHERE g.deleted_at IS NULL
     *   AND (LOWER(p.first_name) LIKE LOWER('%' || ? || '%') OR LOWER(p.last_name) LIKE ...
     *        OR p.first_name_kh LIKE ... OR p.last_name_kh LIKE ... OR p.phone LIKE ...)
     * ORDER BY ... LIMIT ? OFFSET ?
     * </pre>
     * countQuery: the same WHERE with count(g), for the page totals (Spring can't build it by itself with join fetch).
     * The phone is saved as +855969530336: the service drops spaces and a leading "0" from q first (096 953 → 96953).
     *
     * @param q        search text; "" = all
     * @param pageable page number, size and sort
     * @return one page of guardians, with the totals
     */
    @Query(value = """
            select g from Guardian g
            join fetch g.person p
            where g.deletedAt is null
               and (lower(p.firstName) like lower(concat('%', :q, '%'))
                    or lower(p.lastName) like lower(concat('%', :q, '%'))
                    or p.firstNameKh like concat('%', :q, '%')
                    or p.lastNameKh like concat('%', :q, '%')
                    or p.phone like concat('%', :q, '%'))
            """,
            countQuery = """
            select count(g) from Guardian g
            join g.person p
            where g.deletedAt is null
               and (lower(p.firstName) like lower(concat('%', :q, '%'))
                    or lower(p.lastName) like lower(concat('%', :q, '%'))
                    or p.firstNameKh like concat('%', :q, '%')
                    or p.lastNameKh like concat('%', :q, '%')
                    or p.phone like concat('%', :q, '%'))
            """)
    Page<Guardian> search(String q, Pageable pageable);
}
