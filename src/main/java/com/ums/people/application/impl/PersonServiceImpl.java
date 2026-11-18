package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.common.storage.FileStorageService;
import com.ums.common.storage.ImageFiles;
import com.ums.common.util.PhoneNumbers;
import com.ums.people.api.dto.AddressResponse;
import com.ums.people.api.dto.MyProfileUpdateRequest;
import com.ums.people.api.dto.PersonResponse;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.PersonService;
import com.ums.people.application.mapper.PersonMapper;
import com.ums.people.domain.*;
import com.ums.people.infrastructure.GuardianRepository;
import com.ums.people.infrastructure.PersonRepository;
import com.ums.people.infrastructure.StaffRepository;
import com.ums.people.infrastructure.StudentRepository;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * See {@link PersonService}. One copy of the person rules, so students and staff are checked the same way.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final StaffRepository staffRepository;          // my profile: which person is this login?
    private final StudentRepository studentRepository;      // my profile
    private final GuardianRepository guardianRepository;    // my profile
    private final PersonAddressService personAddressService;   // my profile: read and change the addresses
    private final PersonMapper personMapper;                // person → response, form → phone / email
    private final FileStorageService fileStorageService;    // photo: save and delete the file

    /** The biggest photo allowed, in MB. */
    private static final int PHOTO_MAX_MB = 2;

    /** The longest side of a saved photo, in pixels: enough for a profile picture or a student card. */
    private static final int PHOTO_MAX_SIDE = 1024;

    /**
     * Clean → check the format for the nationality → must be free (its own person not counted).
     */
    @Override
    @Transactional(readOnly = true)
    public String requireValidNationalId(String nationalId, String nationalityCode, Long personId) {
        if (nationalId == null || nationalId.isBlank()) {
            return null;   // optional: e.g. under 18, no ID card yet
        }
        String cleaned = nationalId.replaceAll("[\\s-]", "").toUpperCase();

        // 1. The format depends on the nationality
        boolean cambodian = Country.CAMBODIA.equals(nationalityCode);
        if (cambodian ? !cleaned.matches("\\d{9}") : !cleaned.matches("[A-Z0-9]{6,20}")) {
            throw new BusinessException(cambodian ? "error.person.national_id_invalid_kh" : "error.person.national_id_invalid");
        }

        // 2. One person = one national ID (also catches the same person registered twice)
        boolean taken = personId == null
                ? personRepository.existsByNationalIdAndDeletedAtIsNull(cleaned)
                : personRepository.existsByNationalIdAndDeletedAtIsNullAndIdNot(cleaned, personId);
        if (taken) {
            throw new BusinessException("error.person.national_id_taken", cleaned);
        }
        return cleaned;
    }

    /**
     * No phone → nothing to check; otherwise must be free (its own person not counted).
     */
    @Override
    @Transactional(readOnly = true)
    public void requireFreePhone(String phone, Long personId) {
        if (phone == null || phone.isBlank()) {
            return;   // optional
        }

        // 1. One phone = one person (father and mother each need their own number)
        boolean taken = personId == null
                ? personRepository.existsByPhoneAndDeletedAtIsNull(phone)
                : personRepository.existsByPhoneAndDeletedAtIsNullAndIdNot(phone, personId);
        if (taken) {
            throw new BusinessException("error.person.phone_taken", phone);
        }
    }

    /**
     * No email → nothing to check; otherwise must be free, ignoring case (its own person not counted).
     */
    @Override
    @Transactional(readOnly = true)
    public void requireFreeEmail(String email, Long personId) {
        if (email == null || email.isBlank()) {
            return;   // optional
        }

        // 1. One email = one person (father and mother each need their own email)
        String cleaned = email.trim();
        boolean taken = personId == null
                ? personRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(cleaned)
                : personRepository.existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(cleaned, personId);
        if (taken) {
            throw new BusinessException("error.person.email_taken", cleaned);
        }
    }

    /// MY PROFILE ---------------------------------------------------

    /**
     * Find my person from the login → answer with the person and their addresses.
     */
    @Override
    @Transactional(readOnly = true)
    public PersonResponse getMyProfile(Long userId) {

        // 1. My person, or 404 → findMyPerson(userId)
        Person person = findMyPerson(userId);

        // 2. Answer: person + addresses (full area text) → personAddressService.getByPerson(person.getId())
        List<AddressResponse> addresses = personAddressService.getByPerson(person.getId());


        //    → personMapper.toResponse(person, addresses)
        return personMapper.toResponse(person, addresses);
    }

    /**
     * Checks first → contact → current address → answer.
     */
    @Override
    public PersonResponse updateMyProfile(Long userId, MyProfileUpdateRequest request) {

        // 1. My person, or 404 → Person person = findMyPerson(userId)
        Person person = findMyPerson(userId);

        // 2. CHECKS FIRST, before the person changes (a query flushes a changed entity: 409 instead of 400)
        // 2a. Phone free (my own phone allowed) → requireFreePhone(PhoneNumbers.normalizeForSave(request.phone()), person.getId())
        requireFreePhone(PhoneNumbers.normalizeForSave(request.phone()), person.getId());

        // 2b. Email free (my own email allowed) → requireFreeEmail(request.email(), person.getId())
        requireFreeEmail(request.email(), person.getId());

        // 2c. Only the CURRENT address can be changed here, or 400
        //     → request.currentAddress() != null && request.currentAddress().type() != PersonAddress.Type.CURRENT
        //     → BusinessException("error.address.only_current")
        if (request.currentAddress() != null && request.currentAddress().type() != PersonAddress.Type.CURRENT) {
            throw new BusinessException("error.address.only_current");
        }

        // 3. CHANGE ----------------------------------------------------------
        // 3a. Phone and email → personMapper.updateContact(request, person)   (names, national ID ... untouched)
        //     Trim the email: an empty email = null → person.setEmail(blank ? null : email.trim())
        personMapper.updateContact(request, person);
        String email = request.email();
        person.setEmail(email == null || email.isBlank() ? null : email.trim());

        // 3b. The current address, when sent (null = keep it) → personAddressService.replaceOne(person, request.currentAddress())
        if (request.currentAddress() != null) {
            personAddressService.replaceOne(person, request.currentAddress());
        }

        //     No save(): the person was loaded, JPA writes the UPDATE at commit

        // 4. Answer like getMyProfile → personMapper.toResponse(person, personAddressService.getByPerson(person.getId()))
        return personMapper.toResponse(person, personAddressService.getByPerson(person.getId()));
    }

    /// PHOTO --------------------------------------------------------

    /** Find my person from the login → change the photo. */
    @Override
    public PersonResponse updateMyPhoto(Long userId, MultipartFile photo) {
        return changePhoto(findMyPerson(userId), photo);
    }

    /** Find my person from the login → remove the photo. */
    @Override
    public PersonResponse deleteMyPhoto(Long userId) {
        return removePhoto(findMyPerson(userId));
    }

    /** Find the person by id (404) → change the photo. */
    @Override
    public PersonResponse updatePhoto(Long personId, MultipartFile photo) {
        return changePhoto(findPerson(personId), photo);
    }

    /** Find the person by id (404) → remove the photo. */
    @Override
    public PersonResponse deletePhoto(Long personId) {
        return removePhoto(findPerson(personId));
    }

    /// HELPERS ------------------------------------------------------

    /**
     * Check the image → save the new file → point the person to it → delete the old file (after the commit).
     * Shared by updateMyPhoto (self) and updatePhoto (admin).
     */
    private PersonResponse changePhoto(Person person, MultipartFile photo) {

        // 1. CHECK FIRST: really an image (JPG / PNG, read from the bytes), max 2 MB, or 400
        String extension = ImageFiles.require(photo, PHOTO_MAX_MB);

        // 2. Clean it: upright, max 1024 px, WITHOUT the hidden phone data (GPS position of the home ...)
        byte[] cleaned = ImageFiles.clean(photo, extension, PHOTO_MAX_SIDE);

        // 3. Save the new file in S3: persons/7/0c9f2b1e-….jpg (random name: never overwrites, can't be guessed)
        String newKey = fileStorageService.store(cleaned, "persons/" + person.getId(), extension);

        // 4. Remember the old key, then point the person to the new file
        //    No save(): the person was loaded, JPA writes the UPDATE at commit
        String oldKey = person.getPhotoUrl();
        person.setPhotoUrl(newKey);

        // 5. Delete the old file. It waits for the commit: if the save fails, the old photo is still there
        fileStorageService.delete(oldKey);   // null (no photo before) = nothing to delete

        // 6. Answer like getMyProfile (photoUrl becomes a signed link through @FileUrl)
        return personMapper.toResponse(person, personAddressService.getByPerson(person.getId()));
    }

    /** Delete the file (after the commit) and clear the column. No photo = nothing to delete. */
    private PersonResponse removePhoto(Person person) {

        // 1. Delete the file (waits for the commit), then clear the column
        fileStorageService.delete(person.getPhotoUrl());
        person.setPhotoUrl(null);

        // 2. Answer with photoUrl null
        return personMapper.toResponse(person, personAddressService.getByPerson(person.getId()));
    }

    /** A person that is not deleted, or 404. */
    private Person findPerson(Long personId) {
        return personRepository.findByIdAndDeletedAtIsNull(personId)
                .orElseThrow(() -> new NotFoundException("error.person.not_found"));
    }

    /**
     * The person of a login. persons has no user_id: the login is on students, staff or guardians, and all of them
     * point to the same person (a lecturer who also studies is ONE person). Used by getMyProfile and updateMyProfile.
     */
    private Person findMyPerson(Long userId) {

        // 1. Staff? → staffRepository.findByUserIdAndDeletedAtIsNull(userId) → staff.getPerson()
        //    Optional = a box with 0 or 1 staff (one login is at most one staff member):
        //    found → Optional[Staff] (full), not found → Optional.empty (empty). Like Staff? in Dart.
        Optional<Staff> staff = staffRepository.findByUserIdAndDeletedAtIsNull(userId);
        if (staff.isPresent()) {                 // box full? (Dart: staff != null)
            return staff.get().getPerson();      // get() opens the box; only call it after isPresent(), empty → crash
        }

        // 2. Student? (one login can have several student records, all the same person: take the first)
        //    → studentRepository.findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(userId) → first → getPerson()
        //    List, not Optional: one login can have MANY student records (Bachelor, then Master), newest first
        List<Student> students = studentRepository.findByUserIdAndDeletedAtIsNullOrderByAdmissionDateDesc(userId);
        if (!students.isEmpty()) {                     // at least one record?
            return students.getFirst().getPerson();    // the newest one; every record points to the same person
        }

        // 3. Guardian? → guardianRepository.findByUserIdAndDeletedAtIsNull(userId) → guardian.getPerson()
        //    Same as staff: Optional = 0 or 1 guardian for this login (empty for now: guardians have no login yet)
        Optional<Guardian> guardian = guardianRepository.findByUserIdAndDeletedAtIsNull(userId);
        if (guardian.isPresent()) {              // box full?
            return guardian.get().getPerson();   // open it → the guardian's person
        }

        // 4. None of them (e.g. the admin account has no person) → NotFoundException("error.person.profile_not_found")
        //    We only get here when all 3 boxes were empty: steps 1-3 "return" as soon as they find the person.
        //    throw stops the method → 404 "This login has no personal profile" (text from messages.properties)
        throw new NotFoundException("error.person.profile_not_found");
    }
}
