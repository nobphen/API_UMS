package com.ums.academic.calendar.infrastructure;

import com.ums.academic.calendar.domain.Semester;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Database access for semesters (table semesters). More queries come with the Semester feature. */
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    /**
     * Whether the academic year has a semester (not deleted). Used by AcademicYearService.delete:
     * a year with semesters can't be deleted.
     * <pre>
     * SELECT s.id FROM semesters s WHERE s.academic_year_id = ? AND s.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param academicYearId academic year id
     * @return true when the year has at least one semester
     */
    boolean existsByAcademicYear_IdAndDeletedAtIsNull(Long academicYearId);

    /**
     * Whether a semester (not deleted) of the year would be outside new year dates. Used by
     * AcademicYearService.update: the year can't be made shorter than its semesters.
     * <pre>
     * year (new dates):   2026-10-01 .............. 2027-08-31
     * semester 2:                      2027-03-01 ... 2027-09-15   → outside (ends after the year)
     *
     * SELECT EXISTS (SELECT 1 FROM semesters s
     *                WHERE s.academic_year_id = ? AND s.deleted_at IS NULL
     *                  AND (s.start_date < :startDate OR s.end_date > :endDate))
     * </pre>
     *
     * @param academicYearId academic year id
     * @param startDate      the year's new first day
     * @param endDate        the year's new last day
     * @return true when at least one semester starts before or ends after the new dates
     */
    @Query("""
            select count(s) > 0 from Semester s
            where s.academicYear.id = :academicYearId
              and s.deletedAt is null
              and (s.startDate < :startDate or s.endDate > :endDate)
            """)
    boolean existsOutside(Long academicYearId, LocalDate startDate, LocalDate endDate);
}
