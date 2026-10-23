package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.AddressRequest;
import com.ums.people.api.dto.AddressResponse;
import com.ums.people.application.CountryService;
import com.ums.people.application.PersonAddressService;
import com.ums.people.application.mapper.GeoAreaMapper;
import com.ums.people.domain.Country;
import com.ums.people.domain.GeoArea;
import com.ums.people.domain.Person;
import com.ums.people.domain.PersonAddress;
import com.ums.people.infrastructure.CountryRepository;
import com.ums.people.infrastructure.GeoAreaRepository;
import com.ums.people.infrastructure.PersonAddressRepository;
import com.ums.people.infrastructure.PersonRepository;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PersonAddressServiceImpl implements PersonAddressService {

    /** How Cambodians write these units in English addresses. */
    private static final Map<String, String> ENGLISH_UNIT = Map.of("Quarter", "Sangkat", "Section", "Khan", "Capital", "");

    private final PersonAddressRepository personAddressRepository;
    private final PersonRepository personRepository;
    private final GeoAreaRepository geoAreaRepository;
    private final CountryRepository countryRepository;
    private final CountryService countryService;
    private final GeoAreaMapper geoAreaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getByPerson(Long personId) {
        return personAddressRepository.findByPerson_IdOrderByTypeAsc(personId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void replaceAll(Person person, List<AddressRequest> addresses) {
        if (addresses == null) {
            return;
        }
        Set<PersonAddress.Type> seen = EnumSet.noneOf(PersonAddress.Type.class);
        List<PersonAddress> rows = new ArrayList<>();
        for (AddressRequest request : addresses) {
            if (!seen.add(request.type())) {
                throw new BusinessException("Only one " + request.type() + " address is allowed");
            }
            rows.add(toEntity(person, request));
        }
        personAddressRepository.deleteAll(personAddressRepository.findByPerson_IdOrderByTypeAsc(person.getId()));
        personAddressRepository.flush();   // free (person, type) before the new rows go in
        personAddressRepository.saveAll(rows);
    }

    @Override
    public List<AddressResponse> replaceAll(Long personId, List<AddressRequest> addresses) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new NotFoundException("Person not found"));
        replaceAll(person, addresses == null ? List.of() : addresses);
        return getByPerson(personId);
    }

    /// HELPERS ------------------------------------------------------

    private PersonAddress toEntity(Person person, AddressRequest request) {
        PersonAddress address = new PersonAddress();
        address.setPerson(person);
        address.setType(request.type());
        address.setHouseNo(blankToNull(request.houseNo()));
        address.setStreet(blankToNull(request.street()));
        address.setAddressLine(blankToNull(request.addressLine()));
        String countryCode = countryService.require(request.countryCode()).getCode();   // empty = KH
        address.setCountryCode(countryCode);

        if (request.areaId() != null) {
            if (!Country.CAMBODIA.equals(countryCode)) {
                throw new BusinessException("Provinces, districts, communes and villages are only for addresses in Cambodia");
            }
            // Fill every level from the smallest area chosen, so they always belong together
            for (GeoArea area = findArea(request.areaId()); area != null; area = area.getParent()) {
                if (!area.isActive()) {
                    throw new BusinessException(area.getNameEn() + " is no longer in use; choose its new area");
                }
                switch (area.getLevel()) {
                    case VILLAGE -> address.setVillage(area);
                    case COMMUNE -> address.setCommune(area);
                    case DISTRICT -> address.setDistrict(area);
                    case PROVINCE -> address.setProvince(area);
                }
            }
        } else if (address.getAddressLine() == null) {
            throw new BusinessException("Choose the province, district, commune and village, or write the address");
        }
        return address;
    }

    private GeoArea findArea(Long id) {
        return geoAreaRepository.findById(id).orElseThrow(() -> new NotFoundException("Area not found"));
    }

    private AddressResponse toResponse(PersonAddress a) {
        List<GeoArea> areas = new ArrayList<>();
        for (GeoArea area : new GeoArea[] {a.getVillage(), a.getCommune(), a.getDistrict(), a.getProvince()}) {
            if (area != null) {
                areas.add(area);
            }
        }

        List<String> kh = new ArrayList<>();
        List<String> en = new ArrayList<>();
        if (a.getHouseNo() != null) {
            kh.add("ផ្ទះលេខ " + a.getHouseNo());
            en.add("No. " + a.getHouseNo());
        }
        if (a.getStreet() != null) {
            kh.add("ផ្លូវ " + a.getStreet());
            en.add("St. " + a.getStreet());
        }
        for (GeoArea area : areas) {
            // Some names already carry their unit, e.g. the village "ភូមិ ១" / "Phum 1" in Phnom Penh
            String unitKh = Objects.toString(area.getUnitKh(), "");
            boolean named = !unitKh.isEmpty() && area.getNameKh().startsWith(unitKh);
            kh.add(named ? area.getNameKh() : unitKh + area.getNameKh());
            String unit = named || area.getUnitEn() == null ? "" : ENGLISH_UNIT.getOrDefault(area.getUnitEn(), area.getUnitEn());
            en.add(unit.isEmpty() ? area.getNameEn() : area.getNameEn() + " " + unit);
        }
        if (a.getAddressLine() != null) {
            kh.add(a.getAddressLine());
            en.add(a.getAddressLine());
        }
        Country country = countryRepository.findById(a.getCountryCode()).orElse(null);
        String countryKh = country == null ? a.getCountryCode() : country.getNameKh();
        String countryEn = country == null ? a.getCountryCode() : country.getNameEn();
        if (!Country.CAMBODIA.equals(a.getCountryCode())) {   // the country is written out only for addresses abroad
            kh.add(countryKh);
            en.add(countryEn);
        }

        return new AddressResponse(a.getId(), a.getType(), a.getCountryCode(), countryKh, countryEn,
                a.getHouseNo(), a.getStreet(),
                area(a.getVillage()), area(a.getCommune()), area(a.getDistrict()), area(a.getProvince()),
                a.getAddressLine(), String.join(" ", kh), String.join(", ", en));
    }

    private AddressResponse.Area area(GeoArea area) {
        return area == null ? null : geoAreaMapper.toArea(area);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
