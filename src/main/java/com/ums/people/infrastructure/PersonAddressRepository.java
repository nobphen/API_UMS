package com.ums.people.infrastructure;

import com.ums.people.domain.PersonAddress;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
