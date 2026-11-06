package com.ums.people.application;

import com.ums.people.api.dto.AddressRequest;
import com.ums.people.api.dto.AddressResponse;
import com.ums.people.domain.Person;
import java.util.List;

/**
 * Birth place, permanent and current address of a person (student, staff, guardian or applicant).
 * Used by PersonController and StudentService.
 */
public interface PersonAddressService {

    /**
     * The person's addresses with the full text in Khmer and English.
     *
     * @param personId id in persons (not the student or staff id)
     * @return the addresses, ordered by type; empty when they have none
     */
    List<AddressResponse> getByPerson(Long personId);

    /**
     * Replaces all the person's addresses with these. Used when a student (or staff) is created.
     *
     * @param person    the person, already saved
     * @param addresses the new addresses; null leaves the old ones unchanged, an empty list deletes them all
     * @throws com.ums.common.exception.BusinessException error.address.duplicate_type when a type is sent twice;
     *                                                    error.address.areas_only_cambodia, error.address.village_required, error.address.area_inactive,
     *                                                    error.address.empty for a wrong address;
     *                                                    error.country.unknown for a bad country code
     * @throws com.ums.common.exception.NotFoundException error.geo.not_found for an unknown areaId
     */
    void replaceAll(Person person, List<AddressRequest> addresses);

    /**
     * Same, for the persons endpoint: finds the person first and answers with the saved addresses.
     *
     * @param personId  id in persons
     * @param addresses the new addresses; null or empty deletes them all
     * @return the person's addresses after the change
     * @throws com.ums.common.exception.NotFoundException error.person.not_found when the person does not exist
     */
    List<AddressResponse> replaceAll(Long personId, List<AddressRequest> addresses);
}
