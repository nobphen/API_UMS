package com.ums.academic.calendar.application.impl;

import com.ums.academic.calendar.api.dto.SemesterRequest;
import com.ums.academic.calendar.api.dto.SemesterResponse;
import com.ums.academic.calendar.application.SemesterService;
import com.ums.academic.calendar.application.mapper.SemesterMapper;
import com.ums.academic.calendar.domain.AcademicYear;
import com.ums.academic.calendar.domain.Semester;
import com.ums.academic.calendar.infrastructure.AcademicYearRepository;
import com.ums.academic.calendar.infrastructure.SemesterRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.ums.academic.section.infrastructure.ClassSectionRepository;
import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.common.util.Texts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link SemesterService}. Every method runs in one transaction (@Transactional on the class):
 * when a rule fails, nothing is saved. Reads use readOnly = true.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {

    /**
     * create sends this as excludeId to overlaps(): no row has id 0, so no semester is skipped.
     */
    private static final long NEW_SEMESTER = 0L;

    private final SemesterRepository semesterRepository;
    private final AcademicYearRepository academicYearRepository;   // the year the semester belongs to (same module)
    private final ClassSectionRepository classSectionRepository;   // "has class sections?" check for delete (same module)
    private final SemesterMapper semesterMapper;                   // request → entity → response (MapStruct)

    /**
     * New semester: year exists, number free, dates and registration OK, then INSERT (not current).
     */
    @Override
    public SemesterResponse create(SemesterRequest request) {

        // 1. The academic year must exist and not be deleted, or 404
        //    → AcademicYear year = findYear(request.academicYearId());
        AcademicYear year = findYear(request.academicYearId());

        // 2. The number must be free in that year (deleted semesters count too), or 400
        //    → semesterRepository.existsByAcademicYear_IdAndSemesterNo(year.getId(), request.semesterNo())
        //    → BusinessException("error.semester.exists", request.semesterNo())
        if (semesterRepository.existsByAcademicYear_IdAndSemesterNo(year.getId(), request.semesterNo())) {
            throw new BusinessException("error.semester.exists", request.semesterNo());
        }

        // 3. Dates + registration: start before end, inside the year, no overlap, registration OK
        //    → checkDates(year, request, NEW_SEMESTER);
        checkDates(year, request, NEW_SEMESTER);

        // 4. Request → new entity, set the year and the trimmed names
        //    → Semester semester = semesterMapper.toEntity(request);
        //    → semester.setAcademicYear(year);
        //    → semester.setName(request.name().trim());
        //    → semester.setNameKh(Texts.blankToNull(request.nameKh()));
        Semester semester = semesterMapper.toEntity(request);
        semester.setAcademicYear(year);
        semester.setName(request.name().trim());
        semester.setNameKh(Texts.blankToNull(request.nameKh()));

        // 5. INSERT, then answer with the saved semester (it has its id now)
        //    → return semesterMapper.toResponse(semesterRepository.save(semester));
        return semesterMapper.toResponse(semesterRepository.save(semester));
    }

    /**
     * The semesters of one year (not deleted), in order 1, 2, 3, each row → response.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SemesterResponse> list(Long academicYearId) {

        // 1. semesterRepository.findByAcademicYear_IdAndDeletedAtIsNullOrderBySemesterNo(academicYearId)
        //    → .stream().map(semesterMapper::toResponse).toList()   (like AcademicYearServiceImpl.list)
        return semesterRepository.findByAcademicYear_IdAndDeletedAtIsNullOrderBySemesterNo(academicYearId)
                .stream()
                .map(semesterMapper::toResponse)
                .toList();
    }

    /**
     * The current semester, or 404 when none is current yet.
     */
    @Override
    @Transactional(readOnly = true)
    public SemesterResponse getCurrent() {

        // 1. semesterRepository.findByCurrentTrueAndDeletedAtIsNull()
        //    → .map(semesterMapper::toResponse)
        //    → .orElseThrow(() -> new NotFoundException("error.semester.no_current"))
        return semesterRepository.findByCurrentTrueAndDeletedAtIsNull()
                .map(semesterMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("error.semester.no_current"));
    }

    /**
     * One semester by id; 404 when it does not exist or was deleted.
     */
    @Override
    @Transactional(readOnly = true)
    public SemesterResponse get(Long id) {

        // 1. → return semesterMapper.toResponse(findSemester(id));
        Semester semester = findSemester(id);
        return semesterMapper.toResponse(semester);
    }

    /**
     * Check the semester, its (maybe new) year, number and dates, copy the request onto it; JPA saves at commit.
     */
    @Override
    public SemesterResponse update(Long id, SemesterRequest request) {

        // 1. The semester must exist and not be deleted → Semester semester = findSemester(id);
        Semester semester = findSemester(id);

        // 2. The academic year in the request must exist (it can be another year), or 404
        //    → AcademicYear year = findYear(request.academicYearId());
        AcademicYear year = findYear(request.academicYearId());


        // 2b. The current semester can't move to another year (its year must stay the current year), or 400
        //    → if (semester.isCurrent() && !semester.getAcademicYear().getId().equals(year.getId()))
        //    → BusinessException("error.semester.current_move")
        if (semester.isCurrent() && !semester.getAcademicYear().getId().equals(year.getId())) {
            throw new BusinessException("error.semester.current_move");
        }

        // 3. The number must be free in that year; its own number is allowed (IdNot skips this semester)
        //    → semesterRepository.existsByAcademicYear_IdAndSemesterNoAndIdNot(year.getId(), request.semesterNo(), id)
        //    → BusinessException("error.semester.exists", request.semesterNo())
        if (semesterRepository.existsByAcademicYear_IdAndSemesterNoAndIdNot(year.getId(), request.semesterNo(), id)) {
            throw new BusinessException("error.semester.exists", request.semesterNo());
        }

        // 4. Dates + registration, overlap with ANOTHER semester of that year → checkDates(year, request, id);
        checkDates(year, request, id);

        // 5. Copy the request onto the semester (current is not touched), set the year and the trimmed names
        //    → semesterMapper.update(request, semester);
        //    → semester.setAcademicYear(year);
        //    → semester.setName(request.name().trim());
        //    → semester.setNameKh(Texts.blankToNull(request.nameKh()));
        semesterMapper.update(request, semester);
        semester.setAcademicYear(year);
        semester.setName(request.name().trim());
        semester.setNameKh(Texts.blankToNull(request.nameKh()));

        // 6. No save(): the semester was loaded, JPA writes the UPDATE at commit
        //    → return semesterMapper.toResponse(semester);
        return semesterMapper.toResponse(semester);
    }

    /**
     * Switch the old current semester off, this one on. Only a semester of the current year can be current.
     */
    @Override
    public SemesterResponse makeCurrent(Long id) {

        // 1. The semester must exist and not be deleted → Semester semester = findSemester(id);
        Semester semester = findSemester(id);

        // 2. Its academic year must be the current year, or 400
        //    (Semester 1 of 2027-2028 can't be current while 2026-2027 is the current year)
        //    → if (!semester.getAcademicYear().isCurrent())
        //    → BusinessException("error.semester.year_not_current")
        if (!semester.getAcademicYear().isCurrent()) {
            throw new BusinessException("error.semester.year_not_current");
        }

        // 3. The old current semester (if there is one and it is another semester) → current = false
        //    → like AcademicYearServiceImpl.makeCurrent (Way A: Optional + if, or Way B: filter + ifPresent)
        //    → semesterRepository.findByCurrentTrueAndDeletedAtIsNull() ...
        semesterRepository.findByCurrentTrueAndDeletedAtIsNull()
                .filter(old -> !old.getId().equals(id))
                .ifPresent(old -> old.setCurrent(false));

        // 4. This semester → current = true → semester.setCurrent(true);
        semester.setCurrent(true);

        // 5. Answer (UPDATE at commit) → return semesterMapper.toResponse(semester);
        return semesterMapper.toResponse(semester);
    }

    /**
     * Soft delete: set deletedAt. Refused while the semester is current or still has class sections.
     */
    @Override
    public void delete(Long id) {

        // 1. The semester must exist and not be deleted → Semester semester = findSemester(id);
        Semester semester = findSemester(id);

        // 2. The current semester can't be deleted: make another semester current first, or 400
        //    (a deleted row would keep is_current = true, and makeCurrent could never switch it off)
        //    → if (semester.isCurrent()) → BusinessException("error.semester.is_current")
        if (semester.isCurrent()) {
            throw new BusinessException("error.semester.is_current");
        }

        // 3. Still has class sections (not deleted): refuse, or 400
        //    → if (classSectionRepository.existsBySemester_IdAndDeletedAtIsNull(id))
        //    → BusinessException("error.semester.has_class_sections")
        if (classSectionRepository.existsBySemester_IdAndDeletedAtIsNull(id)) {
            throw new BusinessException("error.semester.has_class_sections");
        }

        // 4. Soft delete: the row stays, GET no longer shows it → semester.setDeletedAt(OffsetDateTime.now());
        OffsetDateTime now = OffsetDateTime.now();
        semester.setDeletedAt(now);
    }

    /// HELPERS ------------------------------------------------------

    /**
     * The semester, or 404 when it does not exist or was deleted. Used by get, update, makeCurrent and delete.
     */
    private Semester findSemester(Long id) {

        // 1. → return semesterRepository.findByIdAndDeletedAtIsNull(id)
        //          .orElseThrow(() -> new NotFoundException("error.semester.not_found"));
        return semesterRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.semester.not_found"));
    }

    /**
     * The academic year, or 404 when it does not exist or was deleted. Used by create and update.
     */
    private AcademicYear findYear(Long academicYearId) {

        // 1. → return academicYearRepository.findByIdAndDeletedAtIsNull(academicYearId)
        //          .orElseThrow(() -> new NotFoundException("error.academic_year.not_found"));
        return academicYearRepository.findByIdAndDeletedAtIsNull(academicYearId)
                .orElseThrow(() -> new NotFoundException("error.academic_year.not_found"));
    }

    /**
     * The date rules shared by create and update.
     *
     * @param year      the academic year the semester belongs to
     * @param request   the dates to check
     * @param excludeId the semester being changed (update), or NEW_SEMESTER (create)
     */
    private void checkDates(AcademicYear year, SemesterRequest request, Long excludeId) {

        // 1. startDate must be BEFORE endDate (the same day is wrong too), or 400
        //    → if (!request.startDate().isBefore(request.endDate()))
        //    → BusinessException("error.semester.dates_invalid")
        if (!request.startDate().isBefore(request.endDate())) {
            throw new BusinessException("error.semester.dates_invalid");
        }

        // 2. Inside the academic year: not before the year's start, not after the year's end, or 400
        //    year 2026-10-01 ... 2027-09-30   semester 2026-09-15 ... ✘ (starts before the year)
        //    → if (request.startDate().isBefore(year.getStartDate()) || request.endDate().isAfter(year.getEndDate()))
        //    → BusinessException("error.semester.outside_year", year.getName())
        if (request.startDate().isBefore(year.getStartDate()) || request.endDate().isAfter(year.getEndDate())) {
            throw new BusinessException("error.semester.outside_year", year.getName());
        }

        // 3. No day shared with another semester of the same year, or 400
        //    → semesterRepository.overlaps(year.getId(), request.startDate(), request.endDate(), excludeId)
        //    → BusinessException("error.semester.overlap")
        if (semesterRepository.overlaps(year.getId(), request.startDate(), request.endDate(), excludeId)) {
            throw new BusinessException("error.semester.overlap");
        }

        // 4. Registration (optional): both dates or none, start not after end, end not after the semester's end, or 400
        //    regStart = request.registrationStart(), regEnd = request.registrationEnd()
        //    → only one of them is null                → BusinessException("error.semester.registration_invalid")
        //    → both set and regStart.isAfter(regEnd)   → same error
        //    → both set and regEnd.isAfter(request.endDate()) → same error
        //    (registration may start BEFORE the semester: students register in September for October)
        LocalDate regStart = request.registrationStart();
        LocalDate regEnd = request.registrationEnd();

        // 4a. No registration dates at all → nothing to check (registration is optional)
        if (regStart == null && regEnd == null) {
            return;
        }

        // 4b. Only one date sent (start without end, or end without start) → 400
        if (regStart == null || regEnd == null) {
            throw new BusinessException("error.semester.registration_invalid");
        }

        // 4c. Both sent: start after end, or registration ends after the semester → 400
        if (regStart.isAfter(regEnd) || regEnd.isAfter(request.endDate())) {
            throw new BusinessException("error.semester.registration_invalid");
        }
    }
}
