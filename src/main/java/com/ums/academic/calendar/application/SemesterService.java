package com.ums.academic.calendar.application;

import com.ums.academic.calendar.api.dto.SemesterRequest;
import com.ums.academic.calendar.api.dto.SemesterResponse;
import java.util.List;

/**
 * Semesters (e.g. Semester 1 of 2026-2027): create, read, change, make current, (soft) delete.
 * Used by SemesterController. A semester always sits inside its academic year; only one semester is current.
 */
public interface SemesterService {

    /**
     * Adds a semester to an academic year. It is not current yet (use makeCurrent).
     *
     * @param request academicYearId, name, nameKh, semesterNo, startDate, endDate, registrationStart, registrationEnd
     * @return the new semester, with its id
     * @throws com.ums.common.exception.NotFoundException error.academic_year.not_found
     * @throws com.ums.common.exception.BusinessException error.semester.exists (number taken in that year, deleted ones too);
     *                                                    error.semester.dates_invalid (start not before end);
     *                                                    error.semester.outside_year (not inside the year's dates);
     *                                                    error.semester.overlap (shares days with another semester of the year);
     *                                                    error.semester.registration_invalid
     */
    SemesterResponse create(SemesterRequest request);

    /**
     * The semesters of one academic year, deleted ones left out, in order 1, 2, 3. No paging (2-3 rows).
     *
     * @param academicYearId academic year id
     * @return the semesters; empty when the year has none
     */
    List<SemesterResponse> list(Long academicYearId);

    /**
     * The semester now running.
     *
     * @return the current semester
     * @throws com.ums.common.exception.NotFoundException error.semester.no_current when none is current yet
     */
    SemesterResponse getCurrent();

    /**
     * One semester.
     *
     * @param id semester id
     * @return the semester
     * @throws com.ums.common.exception.NotFoundException error.semester.not_found when it does not exist or was deleted
     */
    SemesterResponse get(Long id);

    /**
     * Replaces all fields. "current" does not change here. The semester may move to another academic year,
     * except the current semester (error.semester.current_move).
     *
     * @param id      semester id
     * @param request the new values; the semester may keep its own number
     * @return the semester after the change
     * @throws com.ums.common.exception.NotFoundException error.semester.not_found, error.academic_year.not_found
     * @throws com.ums.common.exception.BusinessException the same rules as create
     */
    SemesterResponse update(Long id, SemesterRequest request);

    /**
     * Makes this semester the current one; the old current semester is switched off (only one current).
     * The semester's academic year must be the current year.
     *
     * @param id semester id
     * @return the semester, now current
     * @throws com.ums.common.exception.NotFoundException error.semester.not_found
     * @throws com.ums.common.exception.BusinessException error.semester.year_not_current
     */
    SemesterResponse makeCurrent(Long id);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its number can't be used again in that year.
     * Refused while the semester still has class sections (not deleted).
     *
     * @param id semester id
     * @throws com.ums.common.exception.NotFoundException error.semester.not_found
     * @throws com.ums.common.exception.BusinessException error.semester.is_current when it is the current semester,
     *                                                    error.semester.has_class_sections
     */
    void delete(Long id);
}
