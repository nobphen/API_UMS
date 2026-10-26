package com.ums.organization.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.organization.api.dto.FacultyRequest;
import com.ums.organization.api.dto.FacultyResponse;
import com.ums.organization.application.FacultyService;
import com.ums.organization.application.mapper.FacultyMapper;
import com.ums.organization.domain.Faculty;
import com.ums.organization.infrastructure.DepartmentRepository;
import com.ums.organization.infrastructure.FacultyRepository;
import java.time.OffsetDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link FacultyService}. Every method runs in one transaction (@Transactional on the class):
 * when a rule fails, nothing is saved. Reads use readOnly = true, which is a little faster.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;   // to check a faculty is empty before deleting it
    private final FacultyMapper facultyMapper;                 // request → entity → response (MapStruct)

    @Override
    public FacultyResponse create(FacultyRequest request) {

        // 1. Same code style for everyone: " eng " → "ENG"
        String code = normalizeCode(request.code());

        // 2. The code must be free (the database keeps it UNIQUE, deleted faculties included)
        if (facultyRepository.existsByCode(code)) {
            throw new BusinessException("error.faculty.code_exists", code);
        }

        // 3. Copy the request into a new entity, then set the cleaned values
        Faculty faculty = facultyMapper.toEntity(request);
        faculty.setCode(code);
        faculty.setNameKh(blankToNull(request.nameKh()));

        // 4. INSERT, then answer with the saved faculty (it has its id now)
        return facultyMapper.toResponse(facultyRepository.save(faculty));
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse get(Long id) {
        return facultyMapper.toResponse(findFaculty(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FacultyResponse> list(String search, Pageable pageable) {
        String q = search == null ? "" : search.trim();   // null would break the query; "" = all
        return facultyRepository.search(q, pageable).map(facultyMapper::toResponse);   // entity page → response page
    }

    @Override
    public FacultyResponse update(Long id, FacultyRequest request) {

        // 1. The faculty must exist and not be deleted
        Faculty faculty = findFaculty(id);

        // 2. The new code must be free; its own code is allowed (IdNot skips this faculty)
        String code = normalizeCode(request.code());
        if (facultyRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessException("error.faculty.code_exists", code);
        }

        // 3. Copy the request onto the faculty, then set the cleaned values
        facultyMapper.update(request, faculty);
        faculty.setCode(code);
        faculty.setNameKh(blankToNull(request.nameKh()));

        // 4. No save() needed: the faculty was loaded in this transaction, so JPA writes the UPDATE at commit
        return facultyMapper.toResponse(faculty);
    }

    @Override
    public void delete(Long id) {

        // 1. The faculty must exist and not be deleted already
        Faculty faculty = findFaculty(id);

        // 2. Departments still point to it: deleting it would leave them without a faculty
        if (departmentRepository.existsByFacultyIdAndDeletedAtIsNull(id)) {
            throw new BusinessException("error.faculty.has_departments");
        }

        // 3. Soft delete: the row stays, GET no longer shows it (UPDATE at commit, like in update)
        faculty.setDeletedAt(OffsetDateTime.now());
    }

    /** The faculty, or 404 when it does not exist or was deleted. Used by get, update and delete. */
    private Faculty findFaculty(Long id) {
        return facultyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.faculty.not_found"));
    }

    /** " eng " → "ENG", so ENG and eng can't become two faculties. Locale.ROOT: same result on every server. */
    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** "" or "   " → null, so an empty Khmer name is saved as "no value", not as an empty text. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
