package com.ums.people.infrastructure;

import com.ums.people.domain.PersonAddress;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonAddressRepository extends JpaRepository<PersonAddress, Long> {

    List<PersonAddress> findByPerson_IdOrderByTypeAsc(Long personId);
}
