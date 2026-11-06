package com.ums.organization.infrastructure;

import com.ums.organization.domain.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/** Database access for buildings (table buildings). Spring Data writes the SQL from the method names. */
public interface BuildingRepository extends JpaRepository<Building, Long> {

    /**
     * One building, skipping deleted ones.
     * <pre>
     * SELECT b.* FROM buildings b WHERE b.id = ? AND b.deleted_at IS NULL
     * </pre>
     *
     * @param id building id
     * @return the building, or empty when it does not exist or was deleted
     */
    Optional<Building> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Whether a building already has this code, deleted ones included: the database keeps the code UNIQUE
     * even after a soft delete, so a deleted building's code can't be used again.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM buildings b WHERE b.code = ?)
     * </pre>
     *
     * @param code building code, e.g. B1
     * @return true when the code is taken
     */
    boolean existsByCode(String code);

    /**
     * Like {@link #existsByCode(String)}, but ignores the building being updated (it may keep its own code).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM buildings b WHERE b.code = ? AND b.id <> ?)
     * </pre>
     *
     * @param code building code, e.g. B1
     * @param id   id of the building being updated
     * @return true when another building has the code
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Buildings for the list page, skipping deleted ones; q filters by code, English or Khmer name (empty q = all).
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=name,asc).
     *
     * @param q        search text; "" = all
     * @param pageable page number, size and sort
     * @return one page of buildings, with the totals
     */
    @Query("""
            select b from Building b
            where b.deletedAt is null
              and (lower(b.code) like lower(concat('%', :q, '%'))
                   or lower(b.name) like lower(concat('%', :q, '%'))
                   or b.nameKh like concat('%', :q, '%'))
            """)
    Page<Building> search(String q, Pageable pageable);
}
