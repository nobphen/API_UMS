package com.ums.people.infrastructure;

import com.ums.people.domain.Person;
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
}
