package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.NewStaffResponse;
import com.ums.people.api.dto.StaffRequest;
import com.ums.people.api.dto.StaffResponse;
import com.ums.people.api.dto.StaffStatusRequest;
import com.ums.people.api.dto.StaffUpdateRequest;
import com.ums.people.api.dto.StatusHistoryResponse;
import com.ums.people.application.CountryService;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.PersonService;
import com.ums.people.application.StaffService;
import com.ums.people.application.mapper.StaffMapper;
import com.ums.people.domain.Person;
import com.ums.people.domain.Staff;
import com.ums.people.domain.StaffStatusHistory;
import com.ums.people.domain.Student;
import com.ums.people.infrastructure.GuardianRepository;
import com.ums.people.infrastructure.PersonRepository;
import com.ums.people.infrastructure.StaffRepository;
import com.ums.people.infrastructure.StaffStatusHistoryRepository;
import com.ums.people.infrastructure.StudentRepository;
import com.ums.security.user.application.UserService;
import com.ums.security.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * See {@link StaffService}. A staff member is saved in 4 tables (+ user_roles inside UserService):
 * <pre>
 * users            the login (username = staff code)         → UserService
 * persons          name, birth date, nationality             → PersonRepository
 * person_addresses birth place, permanent, current address   → PersonAddressService
 * staff            department, position, type, hire date     → StaffRepository
 * </pre>
 * Every method runs in one transaction (@Transactional on the class): when a rule fails, nothing is saved.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private static final String STAFF_ROLE = "STAFF";         // role code given to EVERY staff login (own leave, payslip, chat)
    private static final String LECTURER_ROLE = "LECTURER";   // role code given to a teaching staff login

    private final StaffRepository staffRepository;
    private final PersonRepository personRepository;
    private final PersonAddressService personAddressService;
    private final UserService userService;       // the login account (security module)
    private final PersonService personService;   // checks the national ID (shared with students)
    private final CountryService countryService; // checks the nationality code
    private final StaffMapper staffMapper;       // request → entity, entity → response (MapStruct)
    private final StudentRepository studentRepository;     // delete: is the person also a student?
    private final GuardianRepository guardianRepository;   // delete: is the person also a guardian?
    private final StaffStatusHistoryRepository staffStatusHistoryRepository;   // changeStatus: one row per change

    /**
     * New staff member: first EVERY check (code, login, department, person, addresses), then the INSERTs
     * (login → person → addresses → staff). A mistake in the form saves nothing.
     */
    @Override
    public NewStaffResponse create(StaffRequest request) {

        // 1. The staff code must be free (it is also the username)
        String staffCode = requireNewCode(request.staffCode());

        // Someone who already has a login (e.g. a graduate hired as a lecturer): see createForExistingLogin
        if (request.existingUserId() != null) {
            return createForExistingLogin(staffCode, request);
        }

        // 2. CHECKS ONLY, nothing saved yet ---------------------------------
        // 2a. Username, email and phone free for the new login
        userService.checkNewAccount(staffCode, request.email(), request.phone());

        // 2b. The department exists (only when departmentId is sent)
        requireDepartment(request.departmentId());

        // 2c. The person: nationality and national ID (built in memory, not saved)
        Person person = buildPerson(request);

        // 2d. Every address
        personAddressService.check(request.addresses());

        // 3. SAVE: everything is checked, so these INSERTs only fail on a real database problem ---------
        // 3a. The login: username = staff code, a temporary password, User.UserType.STAFF, the roles
        //     Roles: everyone → "STAFF"; LECTURER / BOTH → also "LECTURER" (an admin adds office roles later)
        UserService.NewAccount account = userService.createAccount(
                staffCode, request.email(), request.phone(), User.UserType.STAFF, rolesFor(request.staffType())
        );

        // 3b. The person, then their addresses
        personRepository.save(person);
        personAddressService.replaceAll(person, request.addresses());

        // 3c. The staff row, linked to the person and the login
        Staff staff = staffMapper.toStaff(request);
        staff.setStaffCode(staffCode);
        staff.setPerson(person);
        staff.setUserId(account.userId());
        staffRepository.save(staff);

        // 4. Answer with the staff member, the username and the temporary password (shown only this once)
        return new NewStaffResponse(toResponse(staff), account.username(), account.temporaryPassword());
    }

    /**
     * A graduate (or anyone else with a login) is hired: same login, same personal details.
     * Same idea as create: every check first, then the changes.
     */
    private NewStaffResponse createForExistingLogin(String staffCode, StaffRequest request) {
        Long userId = request.existingUserId();

        // 1. CHECKS ONLY, nothing saved yet ---------------------------------
        // 1a. The login is not staff already (one person = one staff row)
        if (staffRepository.findByUserIdAndDeletedAtIsNull(userId).isPresent()) {
            throw new BusinessException("error.staff.already_staff");
        }

        // 1b. The department exists (only when departmentId is sent)
        requireDepartment(request.departmentId());

        // 1c. Reuse the person already on file (name, birth date and photo kept in one place);
        //     only when there is none, build a new one from the form (checked, not saved)
        Person person = studentRepository.findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(userId).stream()
                .findFirst().map(Student::getPerson)
                .orElseGet(() -> buildPerson(request));

        // 1d. Every address
        personAddressService.check(request.addresses());

        // 2. SAVE ------------------------------------------------------------
        // 2a. Keep the same login; it also gets the job roles (404 / locked login stops here, before any INSERT)
        String username = userService.useForNewJob(userId, rolesFor(request.staffType()));

        // 2b. A new person only when none was on file; then the addresses (sent = updated; left out = kept)
        if (person.getId() == null) {
            personRepository.save(person);
        }
        personAddressService.replaceAll(person, request.addresses());

        // 2c. Build and INSERT the new staff row
        Staff staff = staffMapper.toStaff(request);   // departmentId, position, title, staffType, hireDate ...
        staff.setStaffCode(staffCode);
        staff.setPerson(person);
        staff.setUserId(userId);
        staffRepository.save(staff);

        // Same login and password as before, so there is no temporary password to hand out
        return new NewStaffResponse(toResponse(staff), username, null);
    }

    /**
     * One staff member by id; 404 when it does not exist or was deleted.
     */
    @Override
    @Transactional(readOnly = true)
    public StaffResponse get(Long id) {

        // 1. The staff member, or 404
        Staff staff = findStaff(id);

        return toResponse(staff);
    }

    /**
     * The staff record of the logged-in user (one login = one staff row).
     */
    @Override
    @Transactional(readOnly = true)
    public StaffResponse getByUser(Long userId) {

        // 1. The staff row of this login, or 404 (one login = one staff row)
        Staff staff = staffRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(
                () -> new NotFoundException("error.staff.not_found")
        );

        // 2. → response
        return toResponse(staff);
    }

    /**
     * One page: filters by department, type and status (null = all), searches code or name.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StaffResponse> list(String search, Long departmentId, Staff.StaffType staffType,
                                    Staff.Status status, Pageable pageable) {

        // 1. null search → "" (null would break the query; "" = all)
        String q = search == null ? "" : search.trim();

        // 2. One page from the database, each staff → response
        return staffRepository.search(q, departmentId, staffType, status, pageable).map(this::toResponse);
    }

    /**
     * Changes the person (names, gender, dob, nationality, contact) and the job details.
     */
    @Override
    public StaffResponse update(Long id, StaffUpdateRequest request) {

        // 1. The staff member, or 404
        Staff staff = findStaff(id);

        // 2. The person part: every field from the form (an empty field clears it)
        Person person = staff.getPerson();
        staffMapper.updatePerson(request, person);
        person.setNationalityCode(countryService.require(request.nationalityCode()).getCode());   // empty = KH
        person.setNationalId(personService.requireValidNationalId(request.nationalId(), person.getNationalityCode(), person.getId()));

        // 3. The login gets the same email and phone, so "Forgot password?" and SMS login use the new ones
        if (staff.getUserId() != null) {
            userService.updateContact(staff.getUserId(), request.email(), request.phone());
        }

        // 4. The department exists (only when departmentId is sent)
        requireDepartment(request.departmentId());

        // 5. The staff part: department, position, title, staff type, hire date
        staffMapper.updateStaff(request, staff);

        // 6. No save(): JPA writes the UPDATEs at commit
        return toResponse(staff);
    }

    /**
     * Fixes a mistyped staff code; the login's username changes too when it was the old code.
     */
    @Override
    public StaffResponse changeCode(Long id, String staffCode) {

        // 1. The staff member, or 404
        Staff staff = findStaff(id);

        // 2. The new code must be different and free ("lt001" → "LT001" is the same row: no check)
        String oldCode = staff.getStaffCode();
        String newCode = staffCode.trim();
        if (newCode.equals(oldCode)) {
            throw new BusinessException("error.staff.code_same");
        }
        if (!newCode.equalsIgnoreCase(oldCode)) {
            newCode = requireNewCode(newCode);   // also refuses codes of deleted staff: a code is never reused
        }

        // 3. The login: renamed too when its username is the old code
        if (staff.getUserId() != null) {
            userService.renameIfUsername(staff.getUserId(), oldCode, newCode);
        }

        // 4. No save(): JPA writes the UPDATE at commit
        staff.setStaffCode(newCode);
        return toResponse(staff);
    }

    /**
     * Different status → allowed change → dean / head check → lock or unlock the login → set it.
     */
    @Override
    public StaffResponse changeStatus(Long id, StaffStatusRequest request, Long adminId) {

        // 1. The staff member, or 404
        Staff staff = findStaff(id);
        Staff.Status old = staff.getStatus();
        Staff.Status next = request.status();

        // 2. The new status must be different from the current one
        if (old == next) {
            throw new BusinessException("error.staff.status_same", next.name());
        }

        // 3. Is this change allowed? (real life: a retired person can't go on leave)
        if (!canChange(old, next)) {
            throw new BusinessException("error.staff.status_not_allowed", old.name(), next.name());
        }

        // 4. Leaving (RESIGNED, TERMINATED, RETIRED): not while dean or department head.
        //    ON_LEAVE and back to ACTIVE are fine: the dean stays dean
        boolean leaving = isGone(next);
        if (leaving && staffRepository.isDeanOrHead(id)) {
            throw new BusinessException("error.staff.is_dean_or_head");
        }

        // 5. The login: leaving → lock; hired again (from RESIGNED, TERMINATED, RETIRED) → unlock.
        //    ON_LEAVE ↔ ACTIVE: the login stays as it is.
        //    Not locked while the same login still studies (e.g. a lecturer doing a PhD)
        if (staff.getUserId() != null) {
            if (leaving) {
                if (!studentRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(
                        staff.getUserId(), Student.Status.CURRENT)) {
                    userService.lock(staff.getUserId(), adminId);
                }
            } else if (isGone(old)) {
                userService.unlock(staff.getUserId());
            }
        }

        // 6. No save() for the staff row: JPA writes the UPDATE at commit
        staff.setStatus(next);

        // 7. INSERT one history row: old → new status, the reason and who did it
        StaffStatusHistory history = new StaffStatusHistory();
        history.setStaffId(staff.getId());
        history.setOldStatus(old);
        history.setNewStatus(next);
        history.setReason(blankToNull(request.reason()));
        history.setChangedBy(adminId);
        history.setChangedAt(OffsetDateTime.now());
        staffStatusHistoryRepository.save(history);

        // 8. The staff member after the change
        return toResponse(staff);
    }

    /**
     * Every status change of the staff member, newest first; 404 when the staff member does not exist.
     */
    @Override
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getStatusHistory(Long id) {

        // 1. The staff member, or 404
        Staff staff = findStaff(id);

        // 2. Each history row → response
        return staffStatusHistoryRepository.findByStaffIdOrderByChangedAtDescIdDesc(staff.getId())
                .stream()
                .map(h ->
                        new StatusHistoryResponse(
                                h.getId(),
                                h.getOldStatus().name(),
                                h.getNewStatus().name(),
                                h.getReason(),
                                h.getChangedBy(),
                                h.getChangedAt()
                        )
                )
                .toList();
    }

    /**
     * Soft delete: only for a record made by mistake or twice.
     */
    @Override
    public void delete(Long id, Long adminId) {

        // 1. The staff member must exist and not be deleted already (404 otherwise)
        Staff staff = findStaff(id);

        // 2. Only ACTIVE: a real staff member who left uses changeStatus
        if (staff.getStatus() != Staff.Status.ACTIVE) {
            throw new BusinessException("error.staff.delete_not_active", staff.getStatus());
        }

        // 3. Not a faculty dean or department head
        if (staffRepository.isDeanOrHead(id)) {
            throw new BusinessException("error.staff.is_dean_or_head");
        }

        // 4. Soft delete: set deleted_at to now (the row stays, GET no longer shows it)
        OffsetDateTime now = OffsetDateTime.now();
        staff.setDeletedAt(now);

        // 5. Nothing else uses the person (no student record, no guardian)?
        Person person = staff.getPerson();
        if (!hasOtherRecord(person.getId())) {

            // 5a. Soft delete the person too: frees the national ID, so the right record can be added again
            person.setDeletedAt(now);

            // 5b. Lock the login: the deleted staff member must not log in any more
            if (staff.getUserId() != null) {
                userService.lock(staff.getUserId(), adminId);
            }
        }

        // 6. No save(): JPA writes the UPDATEs at commit
        log.info("Staff {} ({}) deleted by user {}", staff.getId(), staff.getStaffCode(), adminId);
    }

    /// HELPERS ------------------------------------------------------

    /**
     * The roles of a staff login. Everyone gets STAFF (cleaner, driver, guard too: own leave, payslip, chat).
     * LECTURER and BOTH teach → also LECTURER. An admin gives the office roles later (REGISTRAR, ACCOUNTANT ...).
     */
    private static List<String> rolesFor(Staff.StaffType staffType) {
        return staffType == Staff.StaffType.OFFICE
                ? List.of(STAFF_ROLE)
                : List.of(STAFF_ROLE, LECTURER_ROLE);
    }

    /**
     * Staff + person → one response (StaffResponse has no addresses).
     */
    private StaffResponse toResponse(Staff staff) {
        return staffMapper.toResponse(staff);
    }

    /**
     * A new person from the form, checked but NOT saved (the caller saves it after all other checks).
     * An empty nationality means Cambodian, an unknown code is refused.
     */
    private Person buildPerson(StaffRequest request) {
        Person person = staffMapper.toPerson(request);
        person.setNationalityCode(countryService.require(request.nationalityCode()).getCode());
        person.setNationalId(personService.requireValidNationalId(request.nationalId(), person.getNationalityCode(), null));
        return person;
    }

    /**
     * The department exists (404) when one is given; no department is OK (it is optional).
     * Checked before saving, so a wrong id never reaches the database as a foreign-key error.
     */
    private void requireDepartment(Long departmentId) {
        if (departmentId != null && !staffRepository.departmentExists(departmentId)) {
            throw new NotFoundException("error.department.not_found");
        }
    }

    /**
     * Whether the person still has another record (not deleted): a student row or a guardian row.
     */
    private boolean hasOtherRecord(Long personId) {
        return studentRepository.existsByPerson_IdAndDeletedAtIsNull(personId)
                || guardianRepository.existsByPerson_IdAndDeletedAtIsNull(personId);
    }

    /**
     * The staff member, or 404 when it does not exist or was deleted (deletedAt set).
     */
    private Staff findStaff(Long id) {
        return staffRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.staff.not_found"));
    }

    /**
     * Which status changes make sense in real life:
     * <pre>
     * ACTIVE                        → ON_LEAVE, RESIGNED, TERMINATED, RETIRED
     * ON_LEAVE                      → ACTIVE (back to work), RESIGNED, TERMINATED, RETIRED
     * RESIGNED, TERMINATED, RETIRED → ACTIVE (hired again)
     * </pre>
     */
    private boolean canChange(Staff.Status from, Staff.Status to) {
        return switch (from) {
            case ACTIVE, ON_LEAVE -> true;   // to any other status
            case RESIGNED, TERMINATED, RETIRED -> to == Staff.Status.ACTIVE;
        };
    }

    /**
     * Whether the status means the person no longer works here (RESIGNED, TERMINATED, RETIRED): the login is locked.
     */
    private static boolean isGone(Staff.Status status) {
        return status == Staff.Status.RESIGNED || status == Staff.Status.TERMINATED || status == Staff.Status.RETIRED;
    }

    /**
     * The code without spaces, or 400 when any staff member already has it (deleted ones too).
     * IgnoreCase: "st001" counts as taken when "ST001" exists.
     */
    private String requireNewCode(String code) {
        String staffCode = code.trim();
        if (staffRepository.existsByStaffCodeIgnoreCase(staffCode)) {
            throw new BusinessException("error.staff.code_exists", staffCode);
        }
        return staffCode;
    }

    /**
     * The text without spaces around it, or null when it is empty (an empty reason is saved as NULL).
     */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
