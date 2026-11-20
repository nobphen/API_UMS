package com.ums.academic.calendar.application;

import com.ums.academic.calendar.api.dto.AcademicYearRequest;
import com.ums.academic.calendar.api.dto.AcademicYearResponse;
import java.util.List;

/**
 * Academic years (e.g. 2026-2027): create, read, change, make current, (soft) delete. Used by AcademicYearController.
 * Only one year is current at a time.
 */
public interface AcademicYearService {

    /**
     * Adds a year. It is not current yet (use makeCurrent).
     *
     * @param request name (2026-2027), startDate, endDate
     * @return the new year, with its id
     * @throws com.ums.common.exception.BusinessException error.academic_year.name_exists (a deleted year counts too);
     *                                                    error.academic_year.dates_invalid (startDate not before endDate);
     *                                                    error.academic_year.overlap (shares days with another year)
     */
    AcademicYearResponse create(AcademicYearRequest request);

    /**
     * Years, deleted ones left out, newest first. No paging (one row per year, used by dropdowns).
     *
     * @param notEnded true = only years whose end date is today or later (today = Cambodia time, from the server),
     *                 for forms about now or the future (intake, semester); false = all years (reports, history)
     * @return the years; empty when there are none
     */
    List<AcademicYearResponse> list(boolean notEnded);

    /**
     * The year now running.
     *
     * @return the current year
     * @throws com.ums.common.exception.NotFoundException error.academic_year.no_current when no year is current yet
     */
    AcademicYearResponse getCurrent();

    /**
     * One year.
     *
     * @param id academic year id
     * @return the year
     * @throws com.ums.common.exception.NotFoundException error.academic_year.not_found when it does not exist or was deleted
     */
    AcademicYearResponse get(Long id);

    /**
     * Replaces the name and dates. "current" does not change here.
     *
     * @param id      academic year id
     * @param request the new values; the year may keep its own name
     * @return the year after the change
     * @throws com.ums.common.exception.NotFoundException error.academic_year.not_found
     * @throws com.ums.common.exception.BusinessException error.academic_year.name_exists, dates_invalid, overlap;
     *                                                    error.academic_year.semester_outside when a semester of the
     *                                                    year would be outside the new dates
     */
    AcademicYearResponse update(Long id, AcademicYearRequest request);

    /**
     * Makes this year the current one; the old current year is switched off (only one current).
     * Already current = nothing changes.
     *
     * @param id academic year id
     * @return the year, now current
     * @throws com.ums.common.exception.NotFoundException error.academic_year.not_found
     */
    AcademicYearResponse makeCurrent(Long id);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its name can't be used again.
     *
     * @param id academic year id
     * @throws com.ums.common.exception.NotFoundException error.academic_year.not_found
     * @throws com.ums.common.exception.BusinessException error.academic_year.has_semesters while it still has semesters
     */
    void delete(Long id);
}
