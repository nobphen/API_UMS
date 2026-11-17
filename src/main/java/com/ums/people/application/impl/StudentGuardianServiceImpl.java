package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.StudentGuardianRequest;
import com.ums.people.api.dto.StudentGuardianResponse;
import com.ums.people.api.dto.StudentGuardianUpdateRequest;
import com.ums.people.application.StudentGuardianService;
import com.ums.people.application.mapper.StudentGuardianMapper;
import com.ums.people.domain.Guardian;
import com.ums.people.domain.StudentGuardian;
import com.ums.people.infrastructure.GuardianRepository;
import com.ums.people.infrastructure.StudentGuardianRepository;
import com.ums.people.infrastructure.StudentRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link StudentGuardianService}. One table: student_guardians (key = student_id + guardian_id).
 * The link has no deleted_at: unlink is a real DELETE (the student and the guardian both stay).
 * Every method runs in one transaction (@Transactional on the class): when a rule fails, nothing is saved.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentGuardianServiceImpl implements StudentGuardianService {

    private final StudentGuardianRepository studentGuardianRepository;
    private final StudentRepository studentRepository;     // the student must exist
    private final GuardianRepository guardianRepository;   // the guardian must exist
    private final StudentGuardianMapper studentGuardianMapper;   // link + guardian → response (MapStruct)

    /**
     * New link: check student, guardian and "not linked yet", fix the primary flag, then INSERT.
     */
    @Override
    public StudentGuardianResponse link(Long studentId, StudentGuardianRequest request) {

        // 1. CHECKS ONLY, nothing saved yet ---------------------------------
        // 1a. The student exists and is not deleted, or 404 → studentRepository.findById(studentId).filter(not deleted)
        //     → NotFoundException("error.student.not_found")
        requireStudent(studentId);

        // 1b. The guardian exists and is not deleted, or 404 → guardianRepository.findByIdAndDeletedAtIsNull(request.guardianId())
        //     → NotFoundException("error.guardian.not_found")
        Guardian guardian = findGuardian(request.guardianId());

        // 1c. Not linked yet, or 400 → studentGuardianRepository.existsById(new StudentGuardian.Id(studentId, guardianId))
        //     → BusinessException("error.student_guardian.exists")   (add this message in the STUDENT GUARDIAN block)
        StudentGuardian.Id id = new StudentGuardian.Id(studentId, request.guardianId());

        if (studentGuardianRepository.existsById(id)) {
            throw new BusinessException("error.student_guardian.exists");
        }


        // 2. THE PRIMARY FLAG ---------------------------------------------
        // 2a. The student's FIRST guardian is always primary (even when the form says false)
        //     → boolean primary = request.primary() || !studentGuardianRepository.existsById_StudentId(studentId)
        boolean primary = request.primary() || !studentGuardianRepository.existsById_StudentId(studentId);

        // 2b. This one is primary → the old primary is not any more (only one per student)
        //     → studentGuardianRepository.clearPrimaryExcept(studentId, request.guardianId())
        if (primary) {
            studentGuardianRepository.clearPrimaryExcept(studentId, request.guardianId());
        }

        // 3. SAVE ------------------------------------------------------------
        // 3a. A NEW object needs save(): new StudentGuardian, setId(new StudentGuardian.Id(studentId, guardianId)),
        //     setRelationship(request.relationship()), setPrimary(...) → studentGuardianRepository.save(link)
        StudentGuardian link = studentGuardianMapper.toEntity(request);   // relationship
        link.setId(id);                                                   // from the URL + form
        link.setPrimary(primary);                                         // decided in 2a
        studentGuardianRepository.save(link);

        // 4. Answer with the link + guardian details → toResponse(link, guardian)
        return toResponse(link, guardian);
    }

    /**
     * The student's guardians, primary first.
     */
    @Override
    @Transactional(readOnly = true)
    public List<StudentGuardianResponse> list(Long studentId) {

        // 1. The student exists and is not deleted, or 404 → "error.student.not_found"
        requireStudent(studentId);

        // 2. The links, primary first → studentGuardianRepository.findById_StudentIdOrderByPrimaryDesc(studentId)
        List<StudentGuardian> links = studentGuardianRepository.findById_StudentIdOrderByPrimaryDesc(studentId);

        // 3. Each link → its guardian (skip a deleted guardian) → toResponse(link, guardian)
        //    Tip: guardianRepository.findAllById(ids) loads them in ONE query, not one per link
        //    3a. The guardian ids of the links → [1, 2]
        List<Long> guardianIds = links.stream()
                .map(link -> link.getId().getGuardianId())
                .toList();

        //log.debug("guardianIds: {}", guardianIds);

        //    3b. All guardians in ONE query, kept by id → {1: Dara, 2: Sophea}
        Map<Long, Guardian> guardians = guardianRepository.findAllById(guardianIds).stream()
                .filter(guardian -> !guardian.isDeleted())
                .collect(Collectors.toMap(Guardian::getId, guardian -> guardian));

       // log.debug("guardians found: {}", guardians.keySet());

        //    3c. Each link + its guardian → one response; a deleted guardian (not in the map) is skipped.
        //        The order of links stays (primary first).
        return links.stream()
                .filter(link -> guardians.containsKey(link.getId().getGuardianId()))
                .map(link -> {
                    Guardian guardian = guardians.get(link.getId().getGuardianId());

//                    log.debug("link {} → guardian id={} name={} {}",
//                            link.getId().getGuardianId(), guardian.getId(),
//                            guardian.getPerson().getFirstName(), guardian.getPerson().getLastName());

                    return toResponse(link, guardian);
                })
                .toList();
    }

    /**
     * Changes relationship and primary of one link.
     */
    @Override
    public StudentGuardianResponse update(Long studentId, Long guardianId, StudentGuardianUpdateRequest request) {

        // 1. The link, or 404 → studentGuardianRepository.findById(new StudentGuardian.Id(studentId, guardianId))
        //    → NotFoundException("error.student_guardian.not_found")
        StudentGuardian link = findLink(studentId, guardianId);

        // 2. Becomes primary → clear the old primary first → studentGuardianRepository.clearPrimaryExcept(studentId, guardianId)
        //    Note: run it BEFORE changing this link (the query flushes changed entities, see the phone fix)
        //    Only when asked: "primary": false must not clear the others (the student would have no primary).
        //    "primary": false on the current primary is ignored: to move the ★, make another guardian primary.
        if (request.primary()) {
            studentGuardianRepository.clearPrimaryExcept(studentId, guardianId);
        }

        // 3. Change the fields → link.setRelationship(...), link.setPrimary(...)
        //    No save(): JPA writes the UPDATE at commit
        studentGuardianMapper.update(request, link);   // relationship
        if (request.primary()) {
            link.setPrimary(true);
        }

        // 4. Answer → load the guardian → toResponse(link, guardian)
        Guardian guardian = findGuardian(guardianId);
        return toResponse(link, guardian);
    }

    /**
     * Removes the link only; the student and the guardian stay.
     */
    @Override
    public void unlink(Long studentId, Long guardianId) {

        // 1. The link, or 404 → findById(new StudentGuardian.Id(studentId, guardianId))
        //    → NotFoundException("error.student_guardian.not_found")
        StudentGuardian link = findLink(studentId, guardianId);
        boolean wasPrimary = link.isPrimary();   // remember it BEFORE the delete

        // 2. Real DELETE (a link has no deleted_at) → studentGuardianRepository.delete(link)
        studentGuardianRepository.delete(link);

        // 3. It was the primary and other guardians are left → make one of them primary
        //    (the school must always have someone to call first)
        //    → studentGuardianRepository.findById_StudentIdOrderByPrimaryDesc(studentId), take the first, setPrimary(true)
        //    The query first flushes the DELETE, so the removed link is not in the result.
        //    No guardian left → nothing to do (the list is empty).
        if (wasPrimary) {
            studentGuardianRepository.findById_StudentIdOrderByPrimaryDesc(studentId)
                    .stream()
                    .findFirst()
                    .ifPresent(next -> next.setPrimary(true));   // no save(): loaded entity, UPDATE at commit
        }

        // 4. Log who was unlinked → log.info("Guardian {} unlinked from student {}", guardianId, studentId)
        log.info("Guardian {} unlinked from student {}", guardianId, studentId);
    }

    /// HELPERS ------------------------------------------------------

    /**
     * The student must exist and not be deleted, or 404. Used by link (1a) and list (1).
     */
    private void requireStudent(Long studentId) {
        if (!studentRepository.existsByIdAndDeletedAtIsNull(studentId)) {
            throw new NotFoundException("error.student.not_found");
        }
    }

    /**
     * The guardian (not deleted), or 404. Used by link (1b), update (4) and unlink.
     */
    private Guardian findGuardian(Long guardianId) {
        return guardianRepository.findByIdAndDeletedAtIsNull(guardianId)
                .orElseThrow(() -> new NotFoundException("error.guardian.not_found"));
    }

    /**
     * The link between this student and this guardian, or 404. Used by update (1) and unlink (1).
     */
    private StudentGuardian findLink(Long studentId, Long guardianId) {
        return studentGuardianRepository.findById(new StudentGuardian.Id(studentId, guardianId))
                .orElseThrow(() -> new NotFoundException("error.student_guardian.not_found"));
    }

    /**
     * Link + guardian → one response (StudentGuardianMapper, MapStruct).
     */
    private StudentGuardianResponse toResponse(StudentGuardian link, Guardian guardian) {
        return studentGuardianMapper.toResponse(link, guardian);
    }
}
