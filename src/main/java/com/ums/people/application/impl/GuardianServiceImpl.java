package com.ums.people.application.impl;

import com.ums.common.util.PhoneNumbers;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.GuardianRequest;
import com.ums.people.api.dto.GuardianResponse;
import com.ums.people.api.dto.GuardianUpdateRequest;
import com.ums.people.application.CountryService;
import com.ums.people.application.GuardianService;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.PersonService;
import com.ums.people.application.mapper.GuardianMapper;
import com.ums.people.domain.Guardian;
import com.ums.people.domain.Person;
import com.ums.people.infrastructure.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * See {@link GuardianService}. A guardian is saved in 3 tables (no login in version 1):
 * <pre>
 * persons          name, birth date, nationality             → PersonRepository
 * person_addresses birth place, permanent, current address   → PersonAddressService
 * guardians        occupation                                → GuardianRepository
 * </pre>
 * Every method runs in one transaction (@Transactional on the class): when a rule fails, nothing is saved.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GuardianServiceImpl implements GuardianService {

    private final GuardianRepository guardianRepository;
    private final PersonRepository personRepository;
    private final PersonAddressService personAddressService;
    private final PersonService personService;     // checks the national ID (shared with students and staff)
    private final CountryService countryService;   // checks the nationality code
    private final GuardianMapper guardianMapper;   // request → entity, entity → response (MapStruct)
    private final StudentGuardianRepository studentGuardianRepository;   // delete: remove the student links
    private final StudentRepository studentRepository;   // delete: is the person also a student?
    private final StaffRepository staffRepository;       // delete: is the person also staff?

    /**
     * New guardian: first EVERY check (person, addresses), then the INSERTs (person → addresses → guardian).
     * A mistake in the form saves nothing.
     */
    @Override
    public GuardianResponse create(GuardianRequest request) {

        // 1. CHECKS ONLY, nothing saved yet ---------------------------------
        // 1a. The person: nationality and national ID (built in memory, not saved)
        Person person = buildPerson(request);

        // 1b. Every address
        personAddressService.check(request.addresses());

        // 2. SAVE: everything is checked, so these INSERTs only fail on a real database problem ---------
        // 2a. The person, then their addresses (an address needs the person's id)
        personRepository.save(person);
        personAddressService.replaceAll(person, request.addresses());

        // 2b. The guardian row, linked to the person
        //     A NEW object needs save() (JPA does not know it yet)
        Guardian guardian = guardianMapper.toGuardian(request);
        guardian.setPerson(person);
        guardianRepository.save(guardian);

        // 3. Answer with the new guardian
        return toResponse(guardian);
    }

    /**
     * One guardian by id; 404 when it does not exist or was deleted.
     */
    @Override
    @Transactional(readOnly = true)
    public GuardianResponse get(Long id) {

        // 1. The guardian, or 404 → findGuardian(id)
        Guardian guardian = findGuardian(id);

        // 2. → response
        return toResponse(guardian);
    }

    /**
     * One page of guardians, searching first or last name (English or Khmer) or phone.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GuardianResponse> list(String search, Pageable pageable) {

        // 1. null search → "" (null would break the query; "" = all)
        String q = search == null ? "" : search.trim();

        // 1a. A phone search (only digits, spaces, "+"): phones are saved as +855969530336,
        //     so drop the spaces and a leading "0" (096 953 → 96953). Names are left as they are.
        if (q.matches("[0-9 +]+")) {
            q = q.replace(" ", "").replaceFirst("^0", "");
        }

        // 2. One page from the database, each guardian → response
        return guardianRepository.search(q, pageable).map(this::toResponse);
    }

    /**
     * Changes the person (names, gender, dob, nationality, contact) and the occupation. Addresses are not changed here.
     */
    @Override
    public GuardianResponse update(Long id, GuardianUpdateRequest request) {

        // 1. The guardian, or 404 → findGuardian(id)
        Guardian guardian = findGuardian(id);

        // 2. CHECKS FIRST, before the person changes: a query flushes a changed loaded entity,
        //    so a taken phone would hit the DB index (409) before our check (400)
        // 2a. Nationality (empty = KH) → countryService.require(...)
        // 2b. National ID (pass person.getId(), so the person's own ID is not "taken") → personService.requireValidNationalId
        // 2c. Phone and email: no other person has them → personService.requireFreePhone / requireFreeEmail
        Person person = guardian.getPerson();
        String nationalityCode = countryService.require(request.nationalityCode()).getCode();
        String nationalId = personService.requireValidNationalId(request.nationalId(), nationalityCode, person.getId());
        personService.requireFreePhone(PhoneNumbers.normalizeForSave(request.phone()), person.getId());
        personService.requireFreeEmail(request.email(), person.getId());   // no other person has this email

        // 2d. The person part: every field from the form (an empty field clears it) → guardianMapper.updatePerson
        guardianMapper.updatePerson(request, person);
        person.setNationalityCode(nationalityCode);
        person.setNationalId(nationalId);

        // 3. The guardian part: occupation → guardianMapper.updateGuardian
        guardianMapper.updateGuardian(request, guardian);

        // 4. No save(): JPA writes the UPDATEs at commit
        return toResponse(guardian);
    }

    /**
     * Soft delete: only for a record made by mistake or twice.
     */
    @Override
    public void delete(Long id, Long adminId) {

        // 1. The guardian must exist and not be deleted already (404 otherwise) → findGuardian(id)
        Guardian guardian = findGuardian(id);

        // 2. Soft delete: set deleted_at to now (the row stays, GET no longer shows it)
        OffsetDateTime now = OffsetDateTime.now();
        guardian.setDeletedAt(now);

        // 3. Remove the links to students (soft delete does not fire ON DELETE CASCADE)
        int links = studentGuardianRepository.deleteByGuardianId(guardian.getId());

        // 4. Nothing else uses the person (no student record, no staff record)?
        Person person = guardian.getPerson();
        if (!hasOtherRecord(person.getId())) {

            // 4a. Soft delete the person too: frees the national ID, so the right record can be added again
            person.setDeletedAt(now);
        }

        // 5. No save(): JPA writes the UPDATEs at commit
        log.info("Guardian {} ({} {}) deleted by user {}, {} student link(s) removed",
                guardian.getId(), person.getFirstName(), person.getLastName(), adminId, links);
    }

    /// HELPERS ------------------------------------------------------

    /**
     * The guardian, or 404 when it does not exist or was deleted (deletedAt set).
     */
    private Guardian findGuardian(Long id) {
        return guardianRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.guardian.not_found"));
    }

    /**
     * A new person from the form, checked but NOT saved (the caller saves it after all other checks).
     * An empty nationality means Cambodian, an unknown code is refused.
     * personId = null: a new person has no id yet, so ANY person with this national ID means "taken".
     */
    private Person buildPerson(GuardianRequest request) {
        Person person = guardianMapper.toPerson(request);
        person.setNationalityCode(countryService.require(request.nationalityCode()).getCode());
        person.setNationalId(personService.requireValidNationalId(request.nationalId(), person.getNationalityCode(), null));
        personService.requireFreePhone(person.getPhone(), null);   // no person has this phone yet
        personService.requireFreeEmail(person.getEmail(), null);   // no person has this email yet
        return person;
    }

    /**
     * Whether the person still has another record (not deleted): a student row or a staff row.
     */
    private boolean hasOtherRecord(Long personId) {
        return studentRepository.existsByPerson_IdAndDeletedAtIsNull(personId)
                || staffRepository.existsByPerson_IdAndDeletedAtIsNull(personId);
    }

    /**
     * Guardian + person → one response (GuardianResponse has no addresses).
     */
    private GuardianResponse toResponse(Guardian guardian) {
        return guardianMapper.toResponse(guardian);
    }
}
