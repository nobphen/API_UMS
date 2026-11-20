package com.ums.academic.calendar.infrastructure;

import com.ums.academic.calendar.domain.AcademicYear;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Database access for academic years (table academic_years). */
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    /**
     * One year that is not deleted. Used by AcademicYearService get, update, makeCurrent and delete (empty = 404).
     * <pre>
     * SELECT a.* FROM academic_years a WHERE a.id = ? AND a.deleted_at IS NULL
     * </pre>
     *
     * @param id academic year id
     * @return the year, or empty when it does not exist or is deleted
     */
    Optional<AcademicYear> findByIdAndDeletedAtIsNull(Long id);

    /**
     * All years that are not deleted, newest first. No paging: one row per year (dropdown).
     * <pre>
     * SELECT a.* FROM academic_years a WHERE a.deleted_at IS NULL ORDER BY a.start_date DESC
     * </pre>
     *
     * @return the years, newest first; empty when there are none
     */
    List<AcademicYear> findByDeletedAtIsNullOrderByStartDateDesc();

    /**
     * Years that are not deleted and not ended yet, newest first. Used by GET /academic-years?notEnded=true.
     * Native SQL (real table and column names), so the method name can stay short.
     *
     * @param today today in Cambodia (DateUtils.today()); a year ending today is still included
     * @return the years, newest first; empty when there are none
     */
    @Query(value = """
            SELECT a.* FROM academic_years a
            WHERE a.deleted_at IS NULL
              AND a.end_date >= :today
            ORDER BY a.start_date DESC
            """, nativeQuery = true)
    List<AcademicYear> findNotEnded(LocalDate today);

    /**
     * The year now running. Used by GET /academic-years/current, and by makeCurrent to switch the old one off.
     * <pre>
     * SELECT a.* FROM academic_years a WHERE a.is_current = TRUE AND a.deleted_at IS NULL
     * </pre>
     *
     * @return the current year, or empty when no year is current yet
     */
    Optional<AcademicYear> findByCurrentTrueAndDeletedAtIsNull();

    /**
     * Whether the name is taken. Deleted years ARE counted on purpose: the database rule name UNIQUE counts them too.
     * Used by AcademicYearService.create.
     * <pre>
     * SELECT a.id FROM academic_years a WHERE a.name = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param name e.g. 2026-2027
     * @return true when a year (deleted or not) has this name
     */
    boolean existsByName(String name);

    /**
     * Same as {@link #existsByName}, but skips the year being changed, so saving its own name again is OK.
     * Used by AcademicYearService.update.
     * <pre>
     * SELECT a.id FROM academic_years a WHERE a.name = ? AND a.id <> ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param name e.g. 2026-2027
     * @param id   the year being changed (not counted)
     * @return true when another year has this name
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Whether another year (not deleted) shares at least one day with startDate..endDate.
     * Two periods overlap when each one starts before the other one ends:
     * <pre>
     * 2025-2026: 2025-10-01 ... 2026-09-30
     * 2026-2027:                 2026-09-15 ... 2027-09-30   → overlap (15-30 Sep 2026 in both)
     *
     * SELECT EXISTS (SELECT 1 FROM academic_years a
     *                WHERE a.deleted_at IS NULL AND a.id <> ?
     *                  AND a.start_date <= :endDate AND a.end_date >= :startDate)
     * </pre>
     *
     * @param startDate first day of the new dates
     * @param endDate   last day of the new dates
     * @param excludeId the year being changed (not counted); create sends 0 (no row has id 0)
     * @return true when the dates overlap another year
     */
    @Query("""
            select count(a) > 0 from AcademicYear a
            where a.deletedAt is null
              and a.id <> :excludeId
              and a.startDate <= :endDate
              and a.endDate >= :startDate
            """)
    boolean overlaps(LocalDate startDate, LocalDate endDate, Long excludeId);
}
