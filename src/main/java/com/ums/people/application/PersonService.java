package com.ums.people.application;

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
}
