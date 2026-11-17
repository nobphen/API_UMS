package com.ums.people.application;

import com.ums.people.api.dto.NewStaffResponse;
import com.ums.people.api.dto.StaffRequest;
import com.ums.people.api.dto.StaffResponse;
import com.ums.people.api.dto.StaffStatusRequest;
import com.ums.people.api.dto.StaffUpdateRequest;
import com.ums.people.api.dto.StatusHistoryResponse;
import com.ums.people.domain.Staff;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Staff: everyone who works at the university, lecturers included. Adding them (with their login), changing
 * them, changing their job status and deleting a record made by mistake. Used by StaffController.
 */
public interface StaffService {

    /**
     * Adds the staff member and their login account (username = staff code, temporary password).
     * With existingUserId (e.g. a graduate hired as a lecturer), the existing login and person are reused
     * instead and no password is made. Everything is checked before the first INSERT, so a wrong form saves nothing.
     *
     * @param request person details, addresses, staff code, department, position, title, staff type, hire date
     * @return the staff member, the username and the temporary password (null for an existing login)
     * @throws com.ums.common.exception.BusinessException error.staff.code_exists when the code is taken;
     *                                                    error.staff.already_staff when the login is already staff;
     *                                                    error.country.unknown for a bad nationality;
     *                                                    error.person.national_id_* for a bad or taken national ID; error.person.phone_taken; error.person.email_taken;
     *                                                    error.user.username_taken / email_taken / phone_taken for the login;
     *                                                    error.address.* for a wrong address
     * @throws com.ums.common.exception.NotFoundException error.department.not_found for an unknown department;
     *                                                    error.user.not_found when existingUserId does not exist;
     *                                                    error.geo.not_found for an unknown areaId
     */
    NewStaffResponse create(StaffRequest request);

    /**
     * One staff member with person details.
     *
     * @param id staff id
     * @return the staff member
     * @throws com.ums.common.exception.NotFoundException error.staff.not_found when it does not exist or was deleted
     */
    StaffResponse get(Long id);

    /**
     * The logged-in user's own staff record (the staff app "my profile").
     *
     * @param userId id of the login (users.id), taken from the token
     * @return the staff record
     * @throws com.ums.common.exception.NotFoundException error.staff.not_found when the user is not staff
     */
    StaffResponse getByUser(Long userId);

    /**
     * One page of staff, deleted ones left out.
     *
     * @param search       filters by staff code, first or last name (English or Khmer); null or empty = all
     * @param departmentId only this department; null = all
     * @param staffType    only this type (LECTURER, OFFICE, BOTH); null = all
     * @param status       only this status; null = all
     * @param pageable     page number, size and sort (?page=1&size=20&sort=staffCode,asc)
     * @return the staff of that page, with the totals
     */
    Page<StaffResponse> list(String search, Long departmentId, Staff.StaffType staffType,
                             Staff.Status status, Pageable pageable);

    /**
     * Changes the person details (in persons) and the job details (in staff).
     * The email and phone are also copied to the login (users), so "Forgot password?" uses the new email.
     *
     * @param id      staff id
     * @param request the new values
     * @return the staff member after the change
     * @throws com.ums.common.exception.NotFoundException error.staff.not_found when it does not exist or was deleted;
     *                                                    error.department.not_found for an unknown department
     * @throws com.ums.common.exception.BusinessException error.country.unknown for a bad nationality;
     *                                                    error.user.email_taken / error.user.phone_taken when
     *                                                    another login already has them;
     *                                                    error.person.national_id_invalid(_kh) / national_id_taken / phone_taken
     */
    StaffResponse update(Long id, StaffUpdateRequest request);

    /**
     * Fixes a mistyped staff code. The login's username changes too when it was the old code (same password).
     * The audit log keeps the old and new code.
     *
     * @param id        staff id
     * @param staffCode the correct code
     * @return the staff member after the change
     * @throws com.ums.common.exception.NotFoundException error.staff.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.staff.code_same when nothing changes;
     *                                                    error.staff.code_exists (also codes of deleted staff);
     *                                                    error.user.username_taken when another login has it
     */
    StaffResponse changeCode(Long id, String staffCode);

    /**
     * A real job event: ON_LEAVE, back to ACTIVE, RESIGNED, TERMINATED or RETIRED.
     * Allowed: ACTIVE → any; ON_LEAVE → any; RESIGNED / TERMINATED / RETIRED → ACTIVE (hired again).
     * RESIGNED, TERMINATED and RETIRED lock the login (not while the same login still studies); back to ACTIVE unlocks it.
     * Every change is also saved in staff_status_history with the reason and the admin.
     *
     * @param id      staff id
     * @param request the new status and the reason (saved in staff_status_history)
     * @param adminId the admin doing it (users.id), needed to lock or unlock the login
     * @return the staff member after the change
     * @throws com.ums.common.exception.NotFoundException error.staff.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.staff.status_same;
     *                                                    error.staff.status_not_allowed;
     *                                                    error.staff.is_dean_or_head when a dean or department
     *                                                    head leaves (choose a new one first);
     *                                                    error.user.lock_self when the admin changes their own record
     */
    StaffResponse changeStatus(Long id, StaffStatusRequest request, Long adminId);

    /**
     * Every status change of the staff member with its reason, newest first (HR: "why did they leave?").
     *
     * @param id staff id
     * @return the changes, newest first; empty when the status never changed
     * @throws com.ums.common.exception.NotFoundException error.staff.not_found when it does not exist or was deleted
     */
    List<StatusHistoryResponse> getStatusHistory(Long id);

    /**
     * Soft delete (sets deleted_at), only for a record made by mistake or twice. Its code can't be used again.
     * A real staff member who leaves uses changeStatus. When the person has no other record (student, guardian),
     * the person is soft deleted and the login locked.
     *
     * @param id      staff id
     * @param adminId the admin doing it (users.id), needed to lock the login
     * @throws com.ums.common.exception.NotFoundException error.staff.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.staff.delete_not_active when the status is not ACTIVE;
     *                                                    error.staff.is_dean_or_head while a faculty dean or
     *                                                    department head;
     *                                                    error.user.lock_self when the admin deletes their own record
     */
    void delete(Long id, Long adminId);
}
