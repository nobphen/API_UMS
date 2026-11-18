package com.ums.people.infrastructure;

import com.ums.people.domain.PersonAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for the addresses of a person (table person_addresses). */
public interface PersonAddressRepository extends JpaRepository<PersonAddress, Long> {

    /**
     * The addresses of one person, ordered by address type.
     * <pre>
     * SELECT pa.* FROM person_addresses pa WHERE pa.person_id = ? ORDER BY pa.address_type ASC
     * </pre>
     *
     * @param personId id of the person
     * @return the person's addresses; empty when they have none
     */
    List<PersonAddress> findByPerson_IdOrderByTypeAsc(Long personId);

    /**
     * One address of one type (a person has at most one of each type). Used by "my profile" to change
     * only the CURRENT address.
     * <pre>
     * SELECT pa.* FROM person_addresses pa WHERE pa.person_id = ? AND pa.address_type = ?
     * </pre>
     *
     * @param personId id of the person
     * @param type     BIRTH_PLACE, PERMANENT or CURRENT
     * @return the address, or empty when the person has none of this type
     */
    Optional<PersonAddress> findByPerson_IdAndType(Long personId, PersonAddress.Type type);
}
