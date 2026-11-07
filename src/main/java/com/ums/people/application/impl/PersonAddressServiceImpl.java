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
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link PersonAddressService}. A person has at most one address of each type (BIRTH_PLACE, PERMANENT, CURRENT).
 * In Cambodia the address stores the village, commune, district and province (filled from the one areaId sent);
 * abroad it stores the country and a free text line.
 */
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

    /** The person's address rows, each turned into a response with the full Khmer and English text. */
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getByPerson(Long personId) {
        return personAddressRepository.findByPerson_IdOrderByTypeAsc(personId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Checks all new addresses first, then deletes the old rows and inserts the new ones. */
    @Override
    public void replaceAll(Person person, List<AddressRequest> addresses) {
        if (addresses == null) {
            return;   // "addresses" not in the request = keep the old ones
        }

        // 1. Check and build every new row first, so one bad address saves nothing
        List<PersonAddress> rows = build(person, addresses);

        // 2. DELETE the old rows, then INSERT the new ones
        personAddressRepository.deleteAll(personAddressRepository.findByPerson_IdOrderByTypeAsc(person.getId()));
        personAddressRepository.flush();   // free (person, type) before the new rows go in
        personAddressRepository.saveAll(rows);
    }

    /** For PUT /persons/{personId}/addresses: find the person (404), replace, then read the addresses back. */
    @Override
    public List<AddressResponse> replaceAll(Long personId, List<AddressRequest> addresses) {
        Person person = personRepository.findById(personId).orElseThrow(() -> new NotFoundException("error.person.not_found"));
        replaceAll(person, addresses == null ? List.of() : addresses);   // here an empty body means "delete all"
        return getByPerson(personId);
    }

    /** The same checks as replaceAll, without a person and without saving: lets a new person be checked before any INSERT. */
    @Override
    public void check(List<AddressRequest> addresses) {
        if (addresses != null) {
            build(null, addresses);   // throws on the first bad address; the rows are thrown away
        }
    }

    /// HELPERS ------------------------------------------------------

    /** Every request → one row after its checks; a type sent twice is refused. Saves nothing. */
    private List<PersonAddress> build(Person person, List<AddressRequest> addresses) {
        Set<PersonAddress.Type> seen = EnumSet.noneOf(PersonAddress.Type.class);
        List<PersonAddress> rows = new ArrayList<>();

        for (AddressRequest request : addresses) {
            if (!seen.add(request.type())) {   // add() is false when the type is already in the set
                throw new BusinessException("error.address.duplicate_type", new DefaultMessageSourceResolvable("address.type." + request.type()));
            }
            rows.add(toEntity(person, request));
        }
        return rows;
    }

    /**
     * One request → one address row, after its checks:
     * areaId only in Cambodia, every area still active, and either an areaId or an addressLine.
     */
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
                throw new BusinessException("error.address.areas_only_cambodia");
            }

            // Must go down to the village: birth certificate, ID card and family book always show it
            GeoArea chosen = findArea(request.areaId());
            if (chosen.getLevel() != GeoArea.Level.VILLAGE) {
                throw new BusinessException("error.address.village_required");
            }

            // Fill every level from the village, so they always belong together.
            // Same walk up as GeoAreaServiceImpl.path: village → commune → district → province → null (stop)
            for (GeoArea area = chosen; area != null; area = area.getParent()) {
                if (!area.isActive()) {
                    throw new BusinessException("error.address.area_inactive", area.getNameEn(), area.getNameKh());
                }
                switch (area.getLevel()) {
                    case VILLAGE -> address.setVillage(area);
                    case COMMUNE -> address.setCommune(area);
                    case DISTRICT -> address.setDistrict(area);
                    case PROVINCE -> address.setProvince(area);
                }
            }
        } else if (address.getAddressLine() == null) {   // no area and no text: nothing to save
            throw new BusinessException("error.address.empty");
        }
        return address;
    }

    /** The area, or 404 when the areaId does not exist. */
    private GeoArea findArea(Long id) {
        return geoAreaRepository.findById(id).orElseThrow(() -> new NotFoundException("error.geo.not_found"));
    }

    /**
     * Address row → response, with the full text built in both languages, smallest part first:
     * <pre>
     * kh: ផ្ទះលេខ 12A ផ្លូវ 271 ភូមិ… ឃុំ… ស្រុក… ខេត្ត…
     * en: No. 12A, St. 271, Phum …, … Commune, … District, … Province
     * </pre>
     */
    private AddressResponse toResponse(PersonAddress a) {
        // The areas that are set, village first
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

    /** One level of the address (id, code, names) for the response; null when that level is empty. */
    private AddressResponse.Area area(GeoArea area) {
        return area == null ? null : geoAreaMapper.toArea(area);
    }

    /** "" or "   " → null, so the database never stores empty text. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
