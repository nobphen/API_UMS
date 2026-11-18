package com.ums.people.application;

import com.ums.people.api.dto.MyProfileUpdateRequest;
import com.ums.people.api.dto.PersonResponse;
import org.springframework.web.multipart.MultipartFile;

/** Shared work on persons (the people behind students, staff and guardians). Used by StudentService and StaffService. */
public interface PersonService {

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
     * @param personId        the person being changed (its own ID is allowed); null for a new person
     * @return the cleaned national ID, or null
     * @throws com.ums.common.exception.BusinessException error.person.national_id_invalid_kh (KH, not 9 digits);
     *                                                    error.person.national_id_invalid (other, wrong format);
     *                                                    error.person.national_id_taken (another person has it)
     */
    String requireValidNationalId(String nationalId, String nationalityCode, Long personId);

    /**
     * Phone is optional; when given no other person (not deleted) may have it, so a call always reaches one person.
     *
     * @param phone    the phone as saved (+855…, already normalized by Person.setPhone); null = no phone
     * @param personId the person being changed (its own phone is allowed); null for a new person
     * @throws com.ums.common.exception.BusinessException error.person.phone_taken (another person has it)
     */
    void requireFreePhone(String phone, Long personId);

    /**
     * Email is optional; when given no other person (not deleted) may have it, ignoring case
     * (Sophea@gmail.com = sophea@gmail.com), so an email always reaches one person.
     *
     * @param email    the email from the form; null or empty = no email
     * @param personId the person being changed (its own email is allowed); null for a new person
     * @throws com.ums.common.exception.BusinessException error.person.email_taken (another person has it)
     */
    void requireFreeEmail(String email, Long personId);

    /// MY PROFILE: the logged-in person (student, staff or guardian) ------------------------

    /**
     * The logged-in person's profile: names, contact, addresses, photo.
     *
     * @param userId the login (from the token, never from the request)
     * @return the person with their addresses
     * @throws com.ums.common.exception.NotFoundException error.person.profile_not_found when the login is not
     *                                                    a student, staff member or guardian (e.g. the admin account)
     */
    PersonResponse getMyProfile(Long userId);

    /**
     * The logged-in person changes their own phone, email and current address. Official data (names, birth date,
     * national ID ...) can't be changed here: only an admin changes them.
     *
     * @param userId  the login (from the token, never from the request)
     * @param request phone, email, current address
     * @return the profile after the change
     * @throws com.ums.common.exception.NotFoundException error.person.profile_not_found (see getMyProfile)
     * @throws com.ums.common.exception.BusinessException error.person.phone_taken / error.person.email_taken;
     *                                                    error.address.only_current when the address type is not CURRENT;
     *                                                    the address errors of PersonAddressService
     */
    PersonResponse updateMyProfile(Long userId, MyProfileUpdateRequest request);

    /// PHOTO: one photo per person (persons.photo_url keeps the file key) ------------------------

    /**
     * The logged-in person changes their own photo. The old photo file is deleted after the save.
     *
     * @param userId the login (from the token)
     * @param photo  the image: JPG or PNG, max 2 MB
     * @return the profile with the new photoUrl
     * @throws com.ums.common.exception.NotFoundException error.person.profile_not_found (see getMyProfile)
     * @throws com.ums.common.exception.BusinessException error.file.empty, error.file.too_large, error.file.not_image
     */
    PersonResponse updateMyPhoto(Long userId, MultipartFile photo);

    /**
     * The logged-in person removes their own photo (no photo = nothing happens).
     *
     * @param userId the login (from the token)
     * @return the profile with photoUrl null
     * @throws com.ums.common.exception.NotFoundException error.person.profile_not_found (see getMyProfile)
     */
    PersonResponse deleteMyPhoto(Long userId);

    /**
     * An admin changes the photo of any person (student, staff, guardian).
     *
     * @param personId id in persons (StudentResponse / StaffResponse have personId)
     * @param photo    the image: JPG or PNG, max 2 MB
     * @return the person with the new photoUrl
     * @throws com.ums.common.exception.NotFoundException error.person.not_found
     * @throws com.ums.common.exception.BusinessException error.file.empty, error.file.too_large, error.file.not_image
     */
    PersonResponse updatePhoto(Long personId, MultipartFile photo);

    /**
     * An admin removes the photo of a person (no photo = nothing happens).
     *
     * @param personId id in persons
     * @return the person with photoUrl null
     * @throws com.ums.common.exception.NotFoundException error.person.not_found
     */
    PersonResponse deletePhoto(Long personId);
}
