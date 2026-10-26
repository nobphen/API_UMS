package com.ums.organization.infrastructure;

import com.ums.organization.domain.Faculty;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    /**
     * One faculty, skipping deleted ones.
     * <pre>
     * SELECT f.* FROM faculties f WHERE f.id = ? AND f.deleted_at IS NULL
     * </pre>
     *
     * @param id faculty id
     * @return the faculty, or empty when it does not exist or was deleted
     */
    Optional<Faculty> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Whether a faculty already has this code, deleted ones included: the database keeps the code UNIQUE
     * even after a soft delete, so a deleted faculty's code can't be used again.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM faculties f WHERE f.code = ?)
     * </pre>
     *
     * @param code faculty code, e.g. ENG
     * @return true when the code is taken
     */
    boolean existsByCode(String code);

    /**
     * Like {@link #existsByCode(String)}, but ignores the faculty being updated (it may keep its own code).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM faculties f WHERE f.code = ? AND f.id <> ?)
     * </pre>
     *
     * @param code faculty code, e.g. ENG
     * @param id   id of the faculty being updated
     * @return true when another faculty has the code
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Faculties for the list page, skipping deleted ones; q filters by code, English or Khmer name
     * (empty q = all). Sorting and page size come from the Pageable (?page=1&size=20&sort=name,asc).
     */
    @Query("""
            select f from Faculty f
            where f.deletedAt is null
              and (lower(f.code) like lower(concat('%', :q, '%'))
                   or lower(f.name) like lower(concat('%', :q, '%'))
                   or f.nameKh like concat('%', :q, '%'))
            """)
    Page<Faculty> search(String q, Pageable pageable);
}
