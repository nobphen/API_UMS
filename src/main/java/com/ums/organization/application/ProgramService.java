package com.ums.organization.application;

import com.ums.organization.api.dto.ProgramRequest;
import com.ums.organization.api.dto.ProgramResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Programs of a department: create, read, change and (soft) delete. Used by ProgramController. */
public interface ProgramService {

    /**
     * Adds a program to a department. The code is saved in capitals (" bcs " → "BCS"); an empty Khmer name is saved as null.
     *
     * @param request department, code, name, Khmer name, degree level, duration, total credits and status
     * @return the new program, with its id
     * @throws com.ums.common.exception.NotFoundException error.department.not_found when the department does not exist
     *                                                    or was deleted
     * @throws com.ums.common.exception.BusinessException error.program.code_exists when any program has the code,
     *                                                    a deleted one included
     */
    ProgramResponse create(ProgramRequest request);

    /**
     * One program.
     *
     * @param id program id
     * @return the program
     * @throws com.ums.common.exception.NotFoundException error.program.not_found when it does not exist or was deleted
     */
    ProgramResponse get(Long id);

    /**
     * One page of programs, deleted ones left out.
     *
     * @param search       filters by code, English or Khmer name; null or empty = all
     * @param departmentId only this department's programs; null = all departments
     * @param pageable     page number, size and sort (?page=1&size=20&sort=name,asc)
     * @return the programs of that page, with the totals
     */
    Page<ProgramResponse> list(String search, Long departmentId, Pageable pageable);

    /**
     * Replaces all the program's fields with the request (code in capitals, empty Khmer name → null).
     * The program can move to another department.
     *
     * @param id      program id
     * @param request the new values; the program may keep its own code
     * @return the program after the change
     * @throws com.ums.common.exception.NotFoundException error.program.not_found when it does not exist or was deleted;
     *                                                    error.department.not_found when the department does not exist
     * @throws com.ums.common.exception.BusinessException error.program.code_exists when another program has the code
     */
    ProgramResponse update(Long id, ProgramRequest request);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its code can't be used again.
     *
     * @param id program id
     * @throws com.ums.common.exception.NotFoundException error.program.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.program.in_use while students, batches, curriculums,
     *                                                    intakes, applications or fee structures use it
     */
    void delete(Long id);
}
