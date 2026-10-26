package com.ums.organization.application;

import com.ums.organization.api.dto.FacultyRequest;
import com.ums.organization.api.dto.FacultyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Faculties of the university: create, read, change and (soft) delete. Used by FacultyController. */
public interface FacultyService {

    /**
     * Adds a faculty. The code is saved in capitals (" eng " → "ENG"); an empty Khmer name is saved as null.
     *
     * @param request code, name, Khmer name and dean
     * @return the new faculty, with its id
     * @throws com.ums.common.exception.BusinessException error.faculty.code_exists when any faculty has the code,
     *                                                    a deleted one included
     */
    FacultyResponse create(FacultyRequest request);

    /**
     * One faculty.
     *
     * @param id faculty id
     * @return the faculty
     * @throws com.ums.common.exception.NotFoundException error.faculty.not_found when it does not exist or was deleted
     */
    FacultyResponse get(Long id);

    /**
     * One page of faculties, deleted ones left out.
     *
     * @param search   filters by code, English or Khmer name; null or empty = all
     * @param pageable page number, size and sort (?page=1&size=20&sort=name,asc)
     * @return the faculties of that page, with the totals
     */
    Page<FacultyResponse> list(String search, Pageable pageable);

    /**
     * Replaces all the faculty's fields with the request (code in capitals, empty Khmer name → null).
     *
     * @param id      faculty id
     * @param request the new values; the faculty may keep its own code
     * @return the faculty after the change
     * @throws com.ums.common.exception.NotFoundException error.faculty.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.faculty.code_exists when another faculty has the code
     */
    FacultyResponse update(Long id, FacultyRequest request);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its code can't be used again.
     *
     * @param id faculty id
     * @throws com.ums.common.exception.NotFoundException error.faculty.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.faculty.has_departments while departments belong to it
     */
    void delete(Long id);
}
