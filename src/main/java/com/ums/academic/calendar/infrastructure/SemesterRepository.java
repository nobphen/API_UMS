package com.ums.academic.calendar.infrastructure;

import com.ums.academic.calendar.domain.Semester;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Database access for semesters (table semesters). */
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    /**
     * One semester that is not deleted. Used by SemesterService get, update, makeCurrent and delete (empty = 404).
     * <pre>
     * SELECT s.* FROM semesters s WHERE s.id = ? AND s.deleted_at IS NULL
     * </pre>
     *
     * @param id semester id
     * @return the semester, or empty when it does not exist or is deleted
     */
    Optional<Semester> findByIdAndDeletedAtIsNull(Long id);

    /**
     * The semesters of one year (not deleted), in order 1, 2, 3. Used by GET /semesters?academicYearId=2.
     * <pre>
     * SELECT s.* FROM semesters s WHERE s.academic_year_id = ? AND s.deleted_at IS NULL ORDER BY s.semester_no
     * </pre>
     *
     * @param academicYearId academic year id
     * @return the semesters; empty when the year has none
     */
    List<Semester> findByAcademicYear_IdAndDeletedAtIsNullOrderBySemesterNo(Long academicYearId);

    /**
     * The semester now running. Used by GET /semesters/current, and by makeCurrent to switch the old one off.
     * <pre>
     * SELECT s.* FROM semesters s WHERE s.is_current = TRUE AND s.deleted_at IS NULL
     * </pre>
     *
     * @return the current semester, or empty when none is current yet
     */
    Optional<Semester> findByCurrentTrueAndDeletedAtIsNull();

    /**
     * Whether the year already has this semester number. Deleted semesters ARE counted on purpose:
     * the database rule UNIQUE (academic_year_id, semester_no) counts them too. Used by SemesterService.create.
     * <pre>
     * SELECT s.id FROM semesters s WHERE s.academic_year_id = ? AND s.semester_no = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param academicYearId academic year id
     * @param semesterNo     1, 2, 3
     * @return true when the number is taken in that year
     */
    boolean existsByAcademicYear_IdAndSemesterNo(Long academicYearId, int semesterNo);

    /**
     * Same as {@link #existsByAcademicYear_IdAndSemesterNo}, but skips the semester being changed,
     * so keeping its own number is OK. Used by SemesterService.update.
     * <pre>
     * SELECT s.id FROM semesters s
     * WHERE s.academic_year_id = ? AND s.semester_no = ? AND s.id <> ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param academicYearId academic year id
     * @param semesterNo     1, 2, 3
     * @param id             the semester being changed (not counted)
     * @return true when another semester of that year has this number
     */
    boolean existsByAcademicYear_IdAndSemesterNoAndIdNot(Long academicYearId, int semesterNo, Long id);

    /**
     * Whether another semester of the SAME year (not deleted) shares at least one day with startDate..endDate.
     * Same rule as AcademicYearRepository.overlaps, but only inside one year.
     * <pre>
     * Semester 1: 2026-10-01 ... 2027-02-28
     * Semester 2:                 2027-02-15 ... 2027-07-31   → overlap (15-28 Feb in both)
     *
     * SELECT EXISTS (SELECT 1 FROM semesters s
     *                WHERE s.academic_year_id = ? AND s.deleted_at IS NULL AND s.id <> ?
     *                  AND s.start_date <= :endDate AND s.end_date >= :startDate)
     * </pre>
     *
     * @param academicYearId academic year id
     * @param startDate      first day of the new dates
     * @param endDate        last day of the new dates
     * @param excludeId      the semester being changed (not counted); create sends 0 (no row has id 0)
     * @return true when the dates overlap another semester of that year
     */
    @Query("""
            select count(s) > 0 from Semester s
            where s.academicYear.id = :academicYearId
              and s.deletedAt is null
              and s.id <> :excludeId
              and s.startDate <= :endDate
              and s.endDate >= :startDate
            """)
    boolean overlaps(Long academicYearId, LocalDate startDate, LocalDate endDate, Long excludeId);

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
