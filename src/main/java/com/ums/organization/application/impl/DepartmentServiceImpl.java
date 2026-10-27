package com.ums.organization.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.organization.api.dto.DepartmentRequest;
import com.ums.organization.api.dto.DepartmentResponse;
import com.ums.organization.application.DepartmentService;
import com.ums.organization.application.mapper.DepartmentMapper;
import com.ums.organization.domain.Department;
import com.ums.organization.domain.Faculty;
import com.ums.organization.infrastructure.DepartmentRepository;
import com.ums.organization.infrastructure.FacultyRepository;
import com.ums.organization.infrastructure.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * See {@link DepartmentService}. Every method runs in one transaction (@Transactional on the class):
 * when a rule fails, nothing is saved. Reads use readOnly = true, which is a little faster.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;   // the department must belong to a faculty that exists
    private final ProgramRepository programRepository;   // to check a department is empty before deleting it
    private final DepartmentMapper departmentMapper;     // request → entity → response (MapStruct)

    @Override
    public DepartmentResponse create(DepartmentRequest request) {

        // 1. Same code style for everyone: " cs " → "CS"
        String code = normalizeCode(request.code());

        // 2. The code must be free (the database keeps it UNIQUE, deleted departments included)
        if (departmentRepository.existsByCode(code)) {
            throw new BusinessException("error.department.code_exists", code);
        }

        // 3. The faculty must exist and not be deleted
        Faculty faculty = findFaculty(request.facultyId());

        // 4. Copy the request into a new entity, then set the faculty and the cleaned values
        Department department = departmentMapper.toEntity(request);
        department.setFaculty(faculty);
        department.setCode(code);
        department.setNameKh(blankToNull(request.nameKh()));

        // 5. INSERT, then answer with the saved department (it has its id now)
        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse get(Long id) {
        return departmentMapper.toResponse(findDepartment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> list(String search, Long facultyId, Pageable pageable) {
        String q = search == null ? "" : search.trim();   // null would break the query; "" = all
        return departmentRepository.search(q, facultyId, pageable).map(departmentMapper::toResponse);   // entity page → response page
    }

    @Override
    public DepartmentResponse update(Long id, DepartmentRequest request) {

        // 1. The department must exist and not be deleted
        Department department = findDepartment(id);

        // 2. The new code must be free; its own code is allowed (IdNot skips this department)
        String code = normalizeCode(request.code());
        if (departmentRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessException("error.department.code_exists", code);
        }

        // 3. The faculty must exist and not be deleted (the department can move to another faculty)
        Faculty faculty = findFaculty(request.facultyId());

        // 4. Copy the request onto the department, then set the faculty and the cleaned values
        departmentMapper.update(request, department);
        department.setFaculty(faculty);
        department.setCode(code);
        department.setNameKh(blankToNull(request.nameKh()));

        // 5. No save() needed: the department was loaded in this transaction, so JPA writes the UPDATE at commit
        return departmentMapper.toResponse(department);
    }

    @Override
    public void delete(Long id) {

        // 1. The department must exist and not be deleted already
        Department department = findDepartment(id);

        // 2. Programs still point to it: deleting it would leave them without a department
        if (programRepository.existsByDepartmentIdAndDeletedAtIsNull(id)) {
            throw new BusinessException("error.department.has_programs");
        }

        // 3. Soft delete: the row stays, GET no longer shows it (UPDATE at commit, like in update)
        department.setDeletedAt(OffsetDateTime.now());
    }

    /** The department, or 404 when it does not exist or was deleted. Used by get, update and delete. */
    private Department findDepartment(Long id) {
        return departmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.department.not_found"));
    }

    /** The faculty, or 404 when it does not exist or was deleted. Used by create and update. */
    private Faculty findFaculty(Long facultyId) {
        return facultyRepository.findByIdAndDeletedAtIsNull(facultyId)
                .orElseThrow(() -> new NotFoundException("error.faculty.not_found"));
    }

    /** " cs " → "CS", so CS and cs can't become two departments. Locale.ROOT: same result on every server. */
    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** "" or "   " → null, so an empty Khmer name is saved as "no value", not as an empty text. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
