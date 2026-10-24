package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.NewProgramRequest;
import com.ums.people.api.dto.NewStudentResponse;
import com.ums.people.api.dto.PromoteStudentsRequest;
import com.ums.people.api.dto.PromoteStudentsResponse;
import com.ums.people.api.dto.StudentRequest;
import com.ums.people.api.dto.StudentResponse;
import com.ums.people.application.CountryService;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.StudentService;
import com.ums.people.application.mapper.StudentMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private static final String STUDENT_ROLE = "STUDENT";

    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;
    private final StaffRepository staffRepository;
    private final StudentMapper studentMapper;
    private final PersonAddressService personAddressService;
    private final CountryService countryService;
    private final UserService userService;

    @Override
    public NewStudentResponse create(StudentRequest request) {
        String studentCode = requireNewCode(request.studentCode());
        if (request.existingUserId() != null) {
            return createForExistingLogin(studentCode, request);
        }

        UserService.NewAccount account = userService.createAccount(
                studentCode, request.email(), request.phone(), User.UserType.STUDENT, STUDENT_ROLE);

        Person person = newPerson(request);
        personAddressService.replaceAll(person, request.addresses());

        Student student = studentMapper.toStudent(request);
        student.setStudentCode(studentCode);
        student.setPerson(person);
        student.setUserId(account.userId());
        student.setYearLevel(request.yearLevel() == null ? 1 : request.yearLevel());
        studentRepository.save(student);

        return new NewStudentResponse(toResponse(student), account.username(), account.temporaryPassword());
    }

    /** A staff member (or anyone else with a login) starts studying: same login, same personal details. */
    private NewStudentResponse createForExistingLogin(String studentCode, StudentRequest request) {
        Long userId = request.existingUserId();
        if (studentRepository.existsByUserIdAndProgramIdAndStatusInAndDeletedAtIsNull(
                userId, request.programId(), Student.Status.CURRENT)) {
            throw new BusinessException("error.student.already_in_program");
        }
        String username = userService.useForNewStudy(userId);

        // Reuse the person already on file, so name, birth date and photo are kept in one place
        Person person = staffRepository.findByUserIdAndDeletedAtIsNull(userId).map(Staff::getPerson)
                .or(() -> studentRepository.findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(userId).stream()
                        .findFirst().map(Student::getPerson))
                .orElseGet(() -> newPerson(request));
        personAddressService.replaceAll(person, request.addresses());   // sent = updated; left out = kept

        Student student = studentMapper.toStudent(request);
        student.setStudentCode(studentCode);
        student.setPerson(person);
        student.setUserId(userId);
        student.setYearLevel(request.yearLevel() == null ? 1 : request.yearLevel());
        studentRepository.save(student);

        // Same login and password as before, so there is no temporary password to hand out
        return new NewStudentResponse(toResponse(student), username, null);
    }

    @Override
    public NewStudentResponse addProgram(Long studentId, NewProgramRequest request) {
        Student existing = findStudent(studentId);
        if (existing.getUserId() == null) {
            throw new BusinessException("error.student.no_login");
        }
        String studentCode = requireNewCode(request.studentCode());
        if (studentRepository.existsByUserIdAndProgramIdAndStatusInAndDeletedAtIsNull(
                existing.getUserId(), request.programId(), Student.Status.CURRENT)) {
            throw new BusinessException("error.student.already_in_program");
        }

        String username = userService.useForNewStudy(existing.getUserId());

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

    @Override
    public PromoteStudentsResponse promote(PromoteStudentsRequest request) {
        Set<Long> keep = request.keepStudentIds() == null ? Set.of() : Set.copyOf(request.keepStudentIds());
        int promoted = 0;
        int kept = 0;
        for (Student student : studentRepository.findByProgramIdAndYearLevelAndStatusAndDeletedAtIsNull(
                request.programId(), request.fromYear(), Student.Status.ACTIVE)) {
            if (request.batchId() != null && !request.batchId().equals(student.getBatchId())) {
                continue;
            }
            if (keep.contains(student.getId())) {
                kept++;   // repeating the year
            } else {
                student.setYearLevel(request.fromYear() + 1);
                promoted++;
            }
        }
        return new PromoteStudentsResponse(promoted, kept);
    }

    @Override
    public StudentResponse setYearLevel(Long id, int yearLevel) {
        Student student = findStudent(id);
        student.setYearLevel(yearLevel);
        return toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        return toResponse(findStudent(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getByUser(Long userId) {
        return studentRepository.findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private StudentResponse toResponse(Student student) {
        return studentMapper.toResponse(student, personAddressService.getByPerson(student.getPerson().getId()));
    }

    /** A new person from the form; an empty nationality means Cambodian, an unknown code is refused. */
    private Person newPerson(StudentRequest request) {
        Person person = studentMapper.toPerson(request);
        person.setNationalityCode(countryService.require(request.nationalityCode()).getCode());
        return personRepository.save(person);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new NotFoundException("error.student.not_found"));
    }

    private String requireNewCode(String code) {
        String studentCode = code.trim();
        if (studentRepository.existsByStudentCodeIgnoreCase(studentCode)) {
            throw new BusinessException("error.student.code_exists", studentCode);
        }
        return studentCode;
    }
}
