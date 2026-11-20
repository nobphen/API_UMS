package com.ums.academic.calendar.application.impl;

import com.ums.academic.calendar.api.dto.AcademicYearRequest;
import com.ums.academic.calendar.api.dto.AcademicYearResponse;
import com.ums.academic.calendar.application.AcademicYearService;
import com.ums.academic.calendar.application.mapper.AcademicYearMapper;
import com.ums.academic.calendar.domain.AcademicYear;
import com.ums.academic.calendar.infrastructure.AcademicYearRepository;
import com.ums.academic.calendar.infrastructure.SemesterRepository;
import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.common.util.DateUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link AcademicYearService}. Every method runs in one transaction (@Transactional on the class):
 * when a rule fails, nothing is saved. Reads use readOnly = true.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AcademicYearServiceImpl implements AcademicYearService {

    /**
     * create sends this as excludeId to overlaps(): no row has id 0, so no year is skipped.
     */
    private static final long NEW_YEAR = 0L;

    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;   // update and delete: the year's semesters
    private final AcademicYearMapper academicYearMapper;   // request → entity → response (MapStruct)

    /**
     * New year: name free, dates OK, no overlap, then INSERT (not current).
     */
    @Override
    public AcademicYearResponse create(AcademicYearRequest request) {

        // 1. Trim the name: " 2026-2027 " → "2026-2027"
        //    → String name = request.name().trim();
        String name = request.name().trim();

        // 2. The name must be free (deleted years count too), or 400
        //    → academicYearRepository.existsByName(name) → BusinessException("error.academic_year.name_exists", name)
        if (academicYearRepository.existsByName(name)) {
            throw new BusinessException("error.academic_year.name_exists", name);
        }

        // 3. Dates: start before end, and no overlap with another year
        //    → checkDates(request.startDate(), request.endDate(), NEW_YEAR)
        checkDates(request.startDate(), request.endDate(), NEW_YEAR);

        // 4. Request → new entity, set the trimmed name
        //    → AcademicYear year = academicYearMapper.toEntity(request); year.setName(name);
        AcademicYear academicYear = academicYearMapper.toEntity(request);
        academicYear.setName(name);

        // 5. INSERT, then answer with the saved year (it has its id now)
        //    → return academicYearMapper.toResponse(academicYearRepository.save(year));
        return academicYearMapper.toResponse(academicYearRepository.save(academicYear));
    }

    /**
     * Years (not deleted), newest first: all of them, or only the ones not ended yet. Each row → response.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> list(boolean notEnded) {

        // 1. notEnded = true  → only years whose end date is today or later
        //                       (today = Cambodia time from the server, never from the client)
        //    notEnded = false → all years
        List<AcademicYear> years = notEnded
                ? academicYearRepository.findNotEnded(DateUtils.today())
                : academicYearRepository.findByDeletedAtIsNullOrderByStartDateDesc();

        // 2. Each row → response   (like BatchServiceImpl.list)
        return years.stream()
                .map(academicYearMapper::toResponse)
                .toList();
    }

    /**
     * The current year, or 404 when none is current yet.
     */
    @Override
    @Transactional(readOnly = true)
    public AcademicYearResponse getCurrent() {

        // 1. academicYearRepository.findByCurrentTrueAndDeletedAtIsNull()
        //    → .map(academicYearMapper::toResponse)
        //    → .orElseThrow(() -> new NotFoundException("error.academic_year.no_current"))
        return academicYearRepository.findByCurrentTrueAndDeletedAtIsNull()
                .map(academicYearMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("error.academic_year.no_current"));
    }

    /**
     * One year by id; 404 when it does not exist or was deleted.
     */
    @Override
    @Transactional(readOnly = true)
    public AcademicYearResponse get(Long id) {

        // 1. → return academicYearMapper.toResponse(findYear(id));
        return academicYearMapper.toResponse(findYear(id));
    }

    /**
     * Check the year, its new name and dates (and its semesters), copy the request onto it; JPA saves at commit.
     */
    @Override
    public AcademicYearResponse update(Long id, AcademicYearRequest request) {

        // 1. The year must exist and not be deleted → AcademicYear year = findYear(id);
        AcademicYear academicYear = findYear(id);

        // 2. The new name must be free; its own name is allowed (IdNot skips this year)
        //    → String name = request.name().trim();
        //    → academicYearRepository.existsByNameAndIdNot(name, id) → BusinessException("error.academic_year.name_exists", name)
        String name = request.name().trim();
        if (academicYearRepository.existsByNameAndIdNot(name, id)) {
            throw new BusinessException("error.academic_year.name_exists", name);
        }

        // 3. Dates: start before end, no overlap with ANOTHER year → checkDates(request.startDate(), request.endDate(), id)
        checkDates(request.startDate(), request.endDate(), id);

        // 4. Its semesters must stay inside the new dates, or 400
        //    → semesterRepository.existsOutside(id, request.startDate(), request.endDate())
        //    → BusinessException("error.academic_year.semester_outside")
        if (semesterRepository.existsOutside(id, request.startDate(), request.endDate())) {
            throw new BusinessException("error.academic_year.semester_outside");
        }

        // 5. Copy the request onto the year (current is not touched), set the trimmed name
        //    → academicYearMapper.update(request, year); year.setName(name);
        academicYearMapper.update(request, academicYear);
        academicYear.setName(name);

        // 6. No save(): the year was loaded, JPA writes the UPDATE at commit
        //    → return academicYearMapper.toResponse(year);
        return academicYearMapper.toResponse(academicYear);
    }

    /**
     * Switch the old current year off, this one on (like the primary guardian).
     */
    @Override
    public AcademicYearResponse makeCurrent(Long id) {

        // 1. The year must exist and not be deleted → AcademicYear year = findYear(id);
        AcademicYear academicYear = findYear(id);

        // 2. The old current year (if there is one and it is another year) → current = false
        //    → academicYearRepository.findByCurrentTrueAndDeletedAtIsNull()
        //        .filter(old -> !old.getId().equals(id))
        //        .ifPresent(old -> old.setCurrent(false));
        //    (setCurrent on a loaded entity, not an UPDATE query: so the change is in the audit log too)

        //    Way A (used): Optional + if
        Optional<AcademicYear> oldCurrent = academicYearRepository.findByCurrentTrueAndDeletedAtIsNull();
        if (oldCurrent.isPresent() && !oldCurrent.get().getId().equals(id)) {
            oldCurrent.get().setCurrent(false);
        }

        //    Way B (same result, shorter): filter + ifPresent
        //    academicYearRepository.findByCurrentTrueAndDeletedAtIsNull()
        //            .filter(old -> !old.getId().equals(id))
        //            .ifPresent(old -> old.setCurrent(false));

        // 3. This year → current = true (always, no "if": a new year is still false here)
        academicYear.setCurrent(true);

        // 4. Answer (UPDATE at commit) → return academicYearMapper.toResponse(year);
        return academicYearMapper.toResponse(academicYear);
    }

    /**
     * Soft delete: refused while the year has semesters, otherwise set deletedAt.
     */
    @Override
    public void delete(Long id) {

        // 1. The year must exist and not be deleted → AcademicYear year = findYear(id);
        AcademicYear academicYear = findYear(id);

        // 2. Still has semesters: refuse (delete them first), or 400
        //    → semesterRepository.existsByAcademicYear_IdAndDeletedAtIsNull(id)
        //    → BusinessException("error.academic_year.has_semesters")
        if (semesterRepository.existsByAcademicYear_IdAndDeletedAtIsNull(id)) {
            throw new BusinessException("error.academic_year.has_semesters");
        }

        // 3. Soft delete: the row stays, GET no longer shows it → year.setDeletedAt(OffsetDateTime.now());
        OffsetDateTime now = OffsetDateTime.now();
        academicYear.setDeletedAt(now);
    }

    /// HELPERS ------------------------------------------------------

    /**
     * The year, or 404 when it does not exist or was deleted. Used by get, update, makeCurrent and delete.
     */
    private AcademicYear findYear(Long id) {

        // 1. → return academicYearRepository.findByIdAndDeletedAtIsNull(id)
        //          .orElseThrow(() -> new NotFoundException("error.academic_year.not_found"));
        return academicYearRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.academic_year.not_found"));
    }

    /**
     * The date rules shared by create and update.
     *
     * @param excludeId the year being changed (update), or NEW_YEAR (create)
     */
    private void checkDates(LocalDate startDate, LocalDate endDate, Long excludeId) {

        // 1. startDate must be BEFORE endDate (the same day is wrong too), or 400
        //    2026-10-01 → 2027-09-30 ✔   2027-09-30 → 2026-10-01 ✘   2026-10-01 → 2026-10-01 ✘
        if (!startDate.isBefore(endDate)) {
            throw new BusinessException("error.academic_year.dates_invalid");
        }

        // 2. No day shared with another year (not deleted, not this year itself), or 400
        if (academicYearRepository.overlaps(startDate, endDate, excludeId)) {
            throw new BusinessException("error.academic_year.overlap");
        }
    }
}
