package com.ums.organization.application.impl;

import com.ums.academic.course.infrastructure.CurriculumRepository;
import com.ums.admission.application.infrastructure.ApplicationRepository;
import com.ums.admission.intake.infrastructure.IntakeProgramRepository;
import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.finance.fee.infrastructure.FeeStructureRepository;
import com.ums.organization.api.dto.ProgramRequest;
import com.ums.organization.api.dto.ProgramResponse;
import com.ums.organization.application.ProgramService;
import com.ums.organization.application.mapper.ProgramMapper;
import com.ums.organization.domain.Department;
import com.ums.organization.domain.Program;
import com.ums.organization.infrastructure.DepartmentRepository;
import com.ums.organization.infrastructure.ProgramRepository;
import com.ums.people.infrastructure.BatchRepository;
import com.ums.people.infrastructure.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * See {@link ProgramService}. Every method runs in one transaction (@Transactional on the class):
 * when a rule fails, nothing is saved. Reads use readOnly = true, which is a little faster.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final ProgramMapper programMapper;

    // Tables that use a program; delete is refused while any of them has a row for it
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final CurriculumRepository curriculumRepository;
    private final IntakeProgramRepository intakeProgramRepository;
    private final ApplicationRepository applicationRepository;
    private final FeeStructureRepository feeStructureRepository;

    @Override
    public ProgramResponse create(ProgramRequest request) {
        // 1. Same code style for everyone: " bcs " → "BCS"
        String code = normalizeCode(request.code());

        // 2. The code must be free (UNIQUE, deleted programs included) → error.program.code_exists
        if (programRepository.existsByCode(code)) {
            throw new BusinessException("error.program.code_exists", code);
        }

        // 3. The department must exist and not be deleted → error.department.not_found
        Department department = findDepartment(request.departmentId());

        // 4. Copy the request into a new entity, then set the department and the cleaned values
        Program program = programMapper.toEntity(request);
        program.setDepartment(department);
        program.setCode(code);
        program.setNameKh(blankToNull(request.nameKh()));
        program.setStatus(statusOrActive(request.status()));

        // 5. INSERT, then answer with the saved program (it has its id now)
        return programMapper.toResponse(programRepository.save(program));
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramResponse get(Long id) {
        // find the program (404 error.program.not_found) → response
        return programMapper.toResponse(findProgram(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProgramResponse> list(String search, Long departmentId, Pageable pageable) {
        // null search → "" (null would break the query), then search and map entity page → response page
        String q = search == null ? "" : search.trim();   // null would break the query; "" = all
        return programRepository.search(q, departmentId, pageable).map(programMapper::toResponse);
    }

    @Override
    public ProgramResponse update(Long id, ProgramRequest request) {
        // 1. The program must exist and not be deleted → error.program.not_found
        Program program = findProgram(id);

        // 2. The new code must be free; its own code is allowed (existsByCodeAndIdNot)
        String code = normalizeCode(request.code());
        if (programRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessException("error.program.code_exists", code);
        }

        // 3. The department must exist and not be deleted (the program can move to another department)
        Department department = findDepartment(request.departmentId());

        // 4. Copy the request onto the program, then set the department and the cleaned values
        programMapper.update(request,program);
        program.setDepartment(department);
        program.setCode(code);
        program.setNameKh(blankToNull(request.nameKh()));
        program.setStatus(statusOrActive(request.status()));

        // 5. No save() needed: JPA writes the UPDATE at commit
        return programMapper.toResponse(program);
    }

    @Override
    public void delete(Long id) {
        // 1. The program must exist and not be deleted already
        Program program = findProgram(id);

        // 2. Still used by students, batches, curriculums, intakes, applications or fees → error.program.in_use
        if (isInUse(id)) {
            throw new BusinessException("error.program.in_use");
        }

        // 3. Soft delete: set deletedAt, the row stays (no save() needed: JPA writes the UPDATE at commit)
        program.setDeletedAt(OffsetDateTime.now());
    }

    /** The program, or 404 when it does not exist or was deleted. Used by get, update and delete. */
    private Program findProgram(Long id) {
        return programRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.program.not_found"));
    }


    /** True when any table still points to this program. Students: deleted ones not counted. */
    private boolean isInUse(Long programId) {
        return studentRepository.existsByProgramIdAndDeletedAtIsNull(programId)
                || batchRepository.existsByProgramId(programId)
                || curriculumRepository.existsByProgramId(programId)
                || intakeProgramRepository.existsByProgramId(programId)
                || applicationRepository.existsByProgramId(programId)
                || feeStructureRepository.existsByProgramId(programId);
    }

    /** The department, or 404 when it does not exist or was deleted. Used by create and update. */
    private Department findDepartment(Long departmentId) {
        return departmentRepository.findByIdAndDeletedAtIsNull(departmentId)
                .orElseThrow(() -> new NotFoundException("error.department.not_found"));
    }

    /** " bcs " → "BCS", so BCS and bcs can't become two programs. Locale.ROOT: same result on every server. */
    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /** "" or "   " → null, so an empty Khmer name is saved as "no value", not as an empty text. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** Empty status → "ACTIVE": the column is NOT NULL, and a new program is open by default. */
    private static String statusOrActive(String status) {
        String value = blankToNull(status);
        return value == null ? "ACTIVE" : value;
    }
}
