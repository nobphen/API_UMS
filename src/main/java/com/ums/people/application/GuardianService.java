package com.ums.people.application;

import com.ums.people.api.dto.GuardianRequest;
import com.ums.people.api.dto.GuardianResponse;
import com.ums.people.api.dto.GuardianUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Guardians (parents) of students: add, read, change and delete them.
 * Version 1: a guardian has no login yet (user_id = null). Linking to students comes later.
 */
public interface GuardianService {

    /**
     * Adds a guardian. Saves 3 tables in this order: persons → person_addresses → guardians.
     * Everything is checked before the first INSERT, so a wrong form saves nothing.
     *
     * @param request person details, addresses and occupation
     * @return the new guardian
     * @throws com.ums.common.exception.BusinessException error.person.national_id_* for a bad or taken national ID; error.person.phone_taken; error.person.email_taken;
     *                                                    error.address.* for a wrong address
     * @throws com.ums.common.exception.NotFoundException error.geo.not_found for an unknown areaId
     */
    GuardianResponse create(GuardianRequest request);

    /**
     * One guardian with person details.
     *
     * @param id guardian id
     * @return the guardian
     * @throws com.ums.common.exception.NotFoundException error.guardian.not_found when it does not exist or was deleted
     */
    GuardianResponse get(Long id);

    /**
     * One page of guardians, deleted ones left out.
     *
     * @param search   filters by first or last name (English or Khmer) or phone; null or empty = all
     * @param pageable page number, size and sort (?page=1&size=20&sort=lastName,asc)
     * @return the guardians of that page, with the totals
     */
    Page<GuardianResponse> list(String search, Pageable pageable);

    /**
     * Changes the person details (in persons) and the occupation (in guardians).
     * Addresses are not changed here: PUT /persons/{personId}/addresses.
     *
     * @param id      guardian id
     * @param request the new values
     * @return the guardian after the change
     * @throws com.ums.common.exception.NotFoundException error.guardian.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.person.national_id_invalid(_kh) / national_id_taken / phone_taken
     */
    GuardianResponse update(Long id, GuardianUpdateRequest request);

    /**
     * Soft delete (sets deleted_at), for a record made by mistake or twice. Its student links are removed.
     * When the person has no other record (student, staff), the person is soft deleted too.
     *
     * @param id      guardian id
     * @param adminId the user who deletes (for the log)
     * @throws com.ums.common.exception.NotFoundException error.guardian.not_found when it does not exist or was deleted
     */
    void delete(Long id, Long adminId);
}
