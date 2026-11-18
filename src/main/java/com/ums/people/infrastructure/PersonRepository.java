package com.ums.people.infrastructure;

import com.ums.people.domain.Person;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for persons (table persons): the people behind students, staff and guardians. */
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Whether a person (not deleted) already has this national ID, for a new person.
     * <pre>
     * SELECT p.id FROM persons p WHERE p.national_id = ? AND p.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param nationalId national ID as saved (no spaces, upper case)
     * @return true when it is taken
     */
    boolean existsByNationalIdAndDeletedAtIsNull(String nationalId);

    /**
     * Whether ANOTHER person (not deleted) already has this national ID, for a change.
     * <pre>
     * SELECT p.id FROM persons p WHERE p.national_id = ? AND p.deleted_at IS NULL AND p.id <> ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param nationalId national ID as saved (no spaces, upper case)
     * @param id         the person who is changing it (not counted)
     * @return true when someone else has it
     */
    boolean existsByNationalIdAndDeletedAtIsNullAndIdNot(String nationalId, Long id);

    /**
     * Whether a person (not deleted) already has this phone, for a new person.
     * <pre>
     * SELECT p.id FROM persons p WHERE p.phone = ? AND p.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param phone phone as saved (+855…)
     * @return true when it is taken
     */
    boolean existsByPhoneAndDeletedAtIsNull(String phone);

    /**
     * Whether ANOTHER person (not deleted) already has this phone, for a change.
     * <pre>
     * SELECT p.id FROM persons p WHERE p.phone = ? AND p.deleted_at IS NULL AND p.id <> ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param phone phone as saved (+855…)
     * @param id    the person who is changing it (not counted)
     * @return true when someone else has it
     */
    boolean existsByPhoneAndDeletedAtIsNullAndIdNot(String phone, Long id);

    /**
     * Whether a person (not deleted) already has this email, ignoring case, for a new person.
     * <pre>
     * SELECT p.id FROM persons p WHERE upper(p.email) = upper(?) AND p.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param email email to check
     * @return true when it is taken
     */
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    /**
     * Whether ANOTHER person (not deleted) already has this email, ignoring case, for a change.
     * <pre>
     * SELECT p.id FROM persons p WHERE upper(p.email) = upper(?) AND p.deleted_at IS NULL AND p.id <> ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param email email to check
     * @param id    the person who is changing it (not counted)
     * @return true when someone else has it
     */
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(String email, Long id);

    /**
     * One person that is not deleted. Used by the admin photo endpoints (empty = 404).
     * <pre>
     * SELECT p.* FROM persons p WHERE p.id = ? AND p.deleted_at IS NULL
     * </pre>
     *
     * @param id person id
     * @return the person, or empty when it does not exist or is deleted
     */
    Optional<Person> findByIdAndDeletedAtIsNull(Long id);
}
