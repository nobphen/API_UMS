package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.GuardianResponse;
import com.ums.people.api.dto.NewProgramRequest;
import com.ums.people.api.dto.NewStudentResponse;
import com.ums.people.api.dto.PromoteStudentsRequest;
import com.ums.people.api.dto.PromoteStudentsResponse;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import com.ums.people.api.dto.StudentStatusRequest;
import com.ums.people.api.dto.StudentUpdateRequest;
import com.ums.people.application.CountryService;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.StudentService;
import com.ums.people.application.mapper.StudentMapper;
import com.ums.people.domain.Country;
import com.ums.people.domain.Person;
import com.ums.people.domain.Staff;
import com.ums.people.domain.Student;
import com.ums.people.infrastructure.PersonRepository;
import com.ums.people.infrastructure.StaffRepository;
import com.ums.people.infrastructure.StudentRepository;
import com.ums.security.user.application.UserService;
import com.ums.security.user.domain.User;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link StudentService}. A student is saved in 4 tables:
 * <pre>
 * users            the login (username = student code)      → UserService
 * persons          name, birth date, nationality            → PersonRepository
 * person_addresses birth place, permanent, current address  → PersonAddressService
 * students         program, batch, year, admission date     → StudentRepository
 * </pre>
 * Every method runs in one transaction (@Transactional on the class): when a rule fails, nothing is saved,
 * not even the login. Reads use readOnly = true, which is a little faster.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private static final String STUDENT_ROLE = "STUDENT";   // role code given to every new student login

    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;
    private final StaffRepository staffRepository;           // to reuse a staff member's person (createForExistingLogin)
    private final StudentMapper studentMapper;               // request → entity, entity → response (MapStruct)
    private final PersonAddressService personAddressService;
    private final CountryService countryService;             // checks the nationality code
    private final UserService userService;                   // the login account (security module)

    /** New student: code check → login → person + addresses → student row (see the steps below). */
    @Override
    public NewStudentResponse create(StudentRequest request) {

        // 1. The student code must be free (it is also the username)
        String studentCode = requireNewCode(request.studentCode());

        // Someone who already has a login (e.g. a lecturer starting a PhD): see createForExistingLogin
        if (request.existingUserId() != null) {
            return createForExistingLogin(studentCode, request);
        }

        // 2. Make the login: username = student code, a temporary password, the STUDENT role
        UserService.NewAccount account = userService.createAccount(
                studentCode, request.email(), request.phone(), User.UserType.STUDENT, STUDENT_ROLE);

        // 3. Save the person (name, birth date, nationality), then their addresses
        Person person = newPerson(request);
        personAddressService.replaceAll(person, request.addresses());

        // 4. Build the student row and link it to the person and the login
        Student student = studentMapper.toStudent(request);   // programId, batchId, admissionDate ...
        student.setStudentCode(studentCode);
        student.setPerson(person);
        student.setUserId(account.userId());
        student.setYearLevel(request.yearLevel() == null ? 1 : request.yearLevel());   // empty = year 1

        // 5. INSERT, then answer with the student, the username and the temporary password (shown only this once)
        studentRepository.save(student);
        return new NewStudentResponse(toResponse(student), account.username(), account.temporaryPassword());
    }

    /** A staff member (or anyone else with a login) starts studying: same login, same personal details. */
    private NewStudentResponse createForExistingLogin(String studentCode, StudentRequest request) {
        Long userId = request.existingUserId();

        // 1. Not already studying this program (an ACTIVE, SUSPENDED or ON_LEAVE record of it)
        if (studentRepository.existsByUserIdAndProgramIdAndStatusInAndDeletedAtIsNull(
                userId, request.programId(), Student.Status.CURRENT)) {
            throw new BusinessException("error.student.already_in_program");
        }

        // 2. Keep the same login; it also gets the STUDENT role
        String username = userService.useForNewStudy(userId);

        // 3. Reuse the person already on file, so name, birth date and photo are kept in one place
        Person person = staffRepository.findByUserIdAndDeletedAtIsNull(userId).map(Staff::getPerson)
                .or(() -> studentRepository.findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(userId).stream()
                        .findFirst().map(Student::getPerson))
                .orElseGet(() -> newPerson(request));
        personAddressService.replaceAll(person, request.addresses());   // sent = updated; left out = kept

        // 4. Build and INSERT the new student row
        Student student = studentMapper.toStudent(request);
        student.setStudentCode(studentCode);
        student.setPerson(person);
        student.setUserId(userId);
        student.setYearLevel(request.yearLevel() == null ? 1 : request.yearLevel());
        studentRepository.save(student);

        // Same login and password as before, so there is no temporary password to hand out
        return new NewStudentResponse(toResponse(student), username, null);
    }

    /** Another program for the same person: a new student row that reuses the person and the login. */
    @Override
    public NewStudentResponse addProgram(Long studentId, NewProgramRequest request) {

        // 1. The existing student record, and it must have a login to share
        Student existing = findStudent(studentId);
        if (existing.getUserId() == null) {
            throw new BusinessException("error.student.no_login");
        }

        // 2. A new, free student code (e.g. MS2030014 for the Master)
        String studentCode = requireNewCode(request.studentCode());

        // 3. Not already studying this program
        if (studentRepository.existsByUserIdAndProgramIdAndStatusInAndDeletedAtIsNull(
                existing.getUserId(), request.programId(), Student.Status.CURRENT)) {
            throw new BusinessException("error.student.already_in_program");
        }

        // 4. Keep the same login
        String username = userService.useForNewStudy(existing.getUserId());

        // 5. A new student row for the same person; the old record stays (e.g. GRADUATED from the Bachelor)
        Student student = new Student();
        student.setStudentCode(studentCode);
        student.setPerson(existing.getPerson());
        student.setUserId(existing.getUserId());
        student.setProgramId(request.programId());
        student.setBatchId(request.batchId());
        student.setAdmissionDate(request.admissionDate());
        student.setYearLevel(request.yearLevel() == null ? 1 : request.yearLevel());
        student.setExpectedGraduationYear(request.expectedGraduationYear());
        studentRepository.save(student);

        // Same login and password as before, so there is no temporary password to hand out
        return new NewStudentResponse(toResponse(student), username, null);
    }

    /** Moves every ACTIVE student of the program (and batch) in fromYear up one year, except the kept ones. */
    @Override
    public PromoteStudentsResponse promote(PromoteStudentsRequest request) {
        // The students repeating the year; a Set makes contains() fast
        Set<Long> keep = request.keepStudentIds() == null ? Set.of() : Set.copyOf(request.keepStudentIds());
        int promoted = 0;
        int kept = 0;

        // Every ACTIVE student of the program in fromYear (SUSPENDED or ON_LEAVE students do not move)
        for (Student student : studentRepository.findByProgramIdAndYearLevelAndStatusAndDeletedAtIsNull(
                request.programId(), request.fromYear(), Student.Status.ACTIVE)) {
            if (request.batchId() != null && !request.batchId().equals(student.getBatchId())) {
                continue;   // another batch: not this time
            }
            // No save(): the students were loaded in this transaction, so JPA writes the UPDATEs at commit
            if (keep.contains(student.getId())) {
                kept++;   // repeating the year
            } else {
                student.setYearLevel(request.fromYear() + 1);
                promoted++;
            }
        }
        return new PromoteStudentsResponse(promoted, kept);
    }

    /** Sets one student's year by hand (the controller already checked 1 to 10). */
    @Override
    public StudentResponse setYearLevel(Long id, int yearLevel) {
        Student student = findStudent(id);
        student.setYearLevel(yearLevel);   // no save() needed: JPA writes the UPDATE at commit
        return toResponse(student);
    }

    /**
     * Still studying → the code really changes → it is free → rename the login when it was the old code → set it.
     * Student is @Auditable, so audit_logs keeps who changed it, when, and the old and new code.
     */
    @Override
    public StudentResponse changeCode(Long id, String studentCode) {

        // 1. The student must exist and still be studying: a graduate's code is printed on the diploma
        Student student = findStudent(id);
        if (!Student.Status.CURRENT.contains(student.getStatus())) {
            throw new BusinessException("error.student.code_not_current");
        }

        // 2. The new code must be different and free ("st2026001" → "ST2026001" is the same row: no check)
        String oldCode = student.getStudentCode();
        String newCode = studentCode.trim();
        if (newCode.equals(oldCode)) {
            throw new BusinessException("error.student.code_same");
        }
        if (!newCode.equalsIgnoreCase(oldCode)) {
            newCode = requireNewCode(newCode);   // also refuses codes of deleted students: a code is never reused
        }

        // 3. The login: renamed too when its username is the old code (a lecturer's own username stays)
        if (student.getUserId() != null) {
            userService.renameIfUsername(student.getUserId(), oldCode, newCode);
        }

        // 4. No save(): JPA writes the UPDATE at commit
        student.setStudentCode(newCode);
        return toResponse(student);
    }

    /** One student by id; 404 when it does not exist or was deleted. */
    @Override
    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        return toResponse(findStudent(id));
    }

    /** All student records of one login (not deleted), newest admission first. */
    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getByUser(Long userId) {
        return studentRepository.findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /// TO DO: write these yourself -----------------------------------
    // Each one throws "not written yet" for now, so a call by mistake gives a clear error instead of null.

    /**
     * One page: filters by program, batch, year and status (null = all), searches code or name,
     * then each student → response with addresses (this::toResponse, because the mapper also needs the addresses).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> list(String search, Long programId, Long batchId, Integer yearLevel,
                                      Student.Status status, Pageable pageable) {

        String q = search == null ? "" : search.trim();   // null would break the query; "" = all

        return studentRepository.search(q, programId, batchId, yearLevel, status, pageable)
                .map(this::toResponse);   // entity page → response page
    }

    /**
     * Changes the person (names, gender, dob, nationality, contact, photo) and the student's batch and
     * expected graduation year. studentCode, program, year level, status and addresses have their own endpoints.
     */
    @Override
    public StudentResponse update(Long id, StudentUpdateRequest request) {

        // 1. The student, or 404
        Student student = findStudent(id);

        // 2. The person part: every field from the form (an empty field clears it, as in a normal form)
        Person person = student.getPerson();
        studentMapper.updatePerson(request, person);
        person.setNationalityCode(countryService.require(request.nationalityCode()).getCode());   // empty = KH
        person.setNationalId(requireValidNationalId(request.nationalId(), person.getNationalityCode(), person.getId()));

        // 3. The login gets the same email and phone, so "Forgot password?" and SMS login use the new ones
        //    (400 when another user already has them; then nothing above is saved either)
        if (student.getUserId() != null) {
            userService.updateContact(student.getUserId(), request.email(), request.phone());
        }

        // 4. The student part
        student.setBatchId(request.batchId());
        student.setExpectedGraduationYear(request.expectedGraduationYear());

        // No save(): both were loaded in this transaction, so JPA writes the UPDATEs at commit
        return toResponse(student);
    }

    /**
     * Different status → allowed change (see canChange) → DROPPED back to ACTIVE only when not already studying
     * the program again → set it. Student is @Auditable, so audit_logs keeps who changed it and old → new status.
     */
    @Override
    public StudentResponse changeStatus(Long id, StudentStatusRequest request) {

        // 1. The student, or 404
        Student student = findStudent(id);
        Student.Status old = student.getStatus();
        Student.Status next = request.status();

        // 2. The new status must be different from the current one
        if (old == next) {
            throw new BusinessException("error.student.status_same", next.name());
        }

        // 3. Is this change allowed? (real life: a graduate can't be suspended)
        if (!canChange(old, next)) {
            throw new BusinessException("error.student.status_not_allowed", old.name(), next.name());
        }

        // 4. DROPPED → ACTIVE: the same login can't study the same program twice at once
        //    (e.g. they were added again as a new student while dropped); the database also has an index for it
        if (old == Student.Status.DROPPED && student.getUserId() != null
                && studentRepository.existsByUserIdAndProgramIdAndStatusInAndDeletedAtIsNull(
                        student.getUserId(), student.getProgramId(), Student.Status.CURRENT)) {
            throw new BusinessException("error.student.already_in_program");
        }

        // 5. No save(): JPA writes the UPDATE at commit.
        //    The reason has no column yet, so for now it only goes to the log next to the audit row
        student.setStatus(next);
        log.info("Student {} ({}) status {} → {}: {}", student.getId(), student.getStudentCode(), old, next, request.reason());

        // 6. The student after the change, so the screen can show the new status at once
        return toResponse(student);
    }

    /** Not written yet. TODO: findStudent → setDeletedAt(now). */
    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("StudentService.delete is not written yet");
    }

    /** Not written yet. TODO: after the Guardian feature: read student_guardians of this student, primary first. */
    @Override
    @Transactional(readOnly = true)
    public List<GuardianResponse> getGuardians(Long id) {
        throw new UnsupportedOperationException("StudentService.getGuardians is not written yet");
    }

    /// HELPERS ------------------------------------------------------

    /** Student + person + addresses → one response. */
    private StudentResponse toResponse(Student student) {
        return studentMapper.toResponse(student, personAddressService.getByPerson(student.getPerson().getId()));
    }

    /** A new person from the form; an empty nationality means Cambodian, an unknown code is refused. */
    private Person newPerson(StudentRequest request) {
        Person person = studentMapper.toPerson(request);
        person.setNationalityCode(countryService.require(request.nationalityCode()).getCode());
        person.setNationalId(requireValidNationalId(request.nationalId(), person.getNationalityCode(), null));
        return personRepository.save(person);
    }

    /** The student, or 404 when it does not exist or was deleted (deletedAt set). */
    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new NotFoundException("error.student.not_found"));
    }

    /**
     * National ID is optional; when given it is cleaned, checked and must be free.
     * <pre>
     * "0102 0304-0"  → "010203040"   spaces and - removed, letters upper case
     * Cambodian (KH) → exactly 9 digits                (ID card)
     * other          → 6 to 20 letters or digits       (passport, e.g. N1234567)
     * </pre>
     *
     * @param nationalId      what was typed (null or empty = no national ID)
     * @param nationalityCode the person's nationality, already checked (KH, TH ...)
     * @param personId        the person being changed; null for a new person
     * @return the cleaned national ID, or null
     */
    private String requireValidNationalId(String nationalId, String nationalityCode, Long personId) {
        if (nationalId == null || nationalId.isBlank()) {
            return null;   // optional: e.g. under 18, no ID card yet
        }
        String cleaned = nationalId.replaceAll("[\\s-]", "").toUpperCase();

        // 1. The format depends on the nationality
        boolean cambodian = Country.CAMBODIA.equals(nationalityCode);
        if (cambodian ? !cleaned.matches("\\d{9}") : !cleaned.matches("[A-Z0-9]{6,20}")) {
            throw new BusinessException(cambodian ? "error.person.national_id_invalid_kh" : "error.person.national_id_invalid");
        }

        // 2. One person = one national ID (also catches the same student registered twice)
        boolean taken = personId == null
                ? personRepository.existsByNationalIdAndDeletedAtIsNull(cleaned)
                : personRepository.existsByNationalIdAndDeletedAtIsNullAndIdNot(cleaned, personId);
        if (taken) {
            throw new BusinessException("error.person.national_id_taken", cleaned);
        }
        return cleaned;
    }

    /**
     * Which status changes make sense in real life:
     * <pre>
     * ACTIVE              → SUSPENDED, ON_LEAVE, GRADUATED, DROPPED
     * SUSPENDED, ON_LEAVE → ACTIVE (back to study), DROPPED
     * DROPPED             → ACTIVE (comes back to the university)
     * GRADUATED           → nothing (final: the diploma is printed)
     * </pre>
     */
    private boolean canChange(Student.Status from, Student.Status to) {
        return switch (from) {
            case ACTIVE -> true;   // to any other status
            case SUSPENDED, ON_LEAVE -> to == Student.Status.ACTIVE || to == Student.Status.DROPPED;
            case DROPPED -> to == Student.Status.ACTIVE;
            case GRADUATED -> false;
        };
    }

    /**
     * The code without spaces, or 400 when any student already has it (deleted ones too).
     * IgnoreCase: "st2026001" counts as taken when "ST2026001" exists.
     */
    private String requireNewCode(String code) {
        String studentCode = code.trim();
        if (studentRepository.existsByStudentCodeIgnoreCase(studentCode)) {
            throw new BusinessException("error.student.code_exists", studentCode);
        }
        return studentCode;
    }
}
