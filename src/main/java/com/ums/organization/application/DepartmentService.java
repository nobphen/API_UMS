package com.ums.organization.application;

import com.ums.organization.api.dto.DepartmentRequest;
import com.ums.organization.api.dto.DepartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Departments of a faculty: create, read, change and (soft) delete. Used by DepartmentController. */
public interface DepartmentService {

    /**
     * Adds a department to a faculty. The code is saved in capitals (" cs " → "CS"); an empty Khmer name is saved as null.
     *
     * @param request faculty, code, name, Khmer name and head of department
     * @return the new department, with its id
     * @throws com.ums.common.exception.NotFoundException error.faculty.not_found when the faculty does not exist
     *                                                    or was deleted
     * @throws com.ums.common.exception.BusinessException error.department.code_exists when any department has the code,
     *                                                    a deleted one included
     */
    DepartmentResponse create(DepartmentRequest request);

    /**
     * One department.
     *
     * @param id department id
     * @return the department
     * @throws com.ums.common.exception.NotFoundException error.department.not_found when it does not exist or was deleted
     */
    DepartmentResponse get(Long id);

    /**
     * One page of departments, deleted ones left out.
     *
     * @param search    filters by code, English or Khmer name; null or empty = all
     * @param facultyId only this faculty's departments; null = all faculties
     * @param pageable  page number, size and sort (?page=1&size=20&sort=name,asc)
     * @return the departments of that page, with the totals
     */
    Page<DepartmentResponse> list(String search, Long facultyId, Pageable pageable);

    /**
     * Replaces all the department's fields with the request (code in capitals, empty Khmer name → null).
     * The department can move to another faculty.
     *
     * @param id      department id
     * @param request the new values; the department may keep its own code
     * @return the department after the change
     * @throws com.ums.common.exception.NotFoundException error.department.not_found when it does not exist or was deleted;
     *                                                    error.faculty.not_found when the faculty does not exist
     * @throws com.ums.common.exception.BusinessException error.department.code_exists when another department has the code
     */
    DepartmentResponse update(Long id, DepartmentRequest request);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its code can't be used again.
     *
     * @param id department id
     * @throws com.ums.common.exception.NotFoundException error.department.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.department.has_programs while programs belong to it
     */
    void delete(Long id);
}
