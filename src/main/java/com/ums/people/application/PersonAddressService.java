package com.ums.people.application;

import com.ums.people.api.dto.AddressRequest;
import com.ums.people.api.dto.AddressResponse;
import com.ums.people.domain.Person;
import java.util.List;

/** Birth place, permanent and current address of a person (student, staff, guardian or applicant). */
public interface PersonAddressService {

    List<AddressResponse> getByPerson(Long personId);

    /** Replaces all the person's addresses with these; null leaves them unchanged. */
    void replaceAll(Person person, List<AddressRequest> addresses);

    /** Same, for the persons endpoint. */
    List<AddressResponse> replaceAll(Long personId, List<AddressRequest> addresses);
}
