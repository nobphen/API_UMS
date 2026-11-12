package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.people.application.PersonService;
import com.ums.people.domain.Country;
import com.ums.people.infrastructure.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** See {@link PersonService}. One copy of the person rules, so students and staff are checked the same way. */
@Service
@Transactional
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    /** Clean → check the format for the nationality → must be free (its own person not counted). */
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
}
