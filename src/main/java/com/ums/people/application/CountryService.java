package com.ums.people.application;

import com.ums.people.api.dto.CountryResponse;
import com.ums.people.domain.Country;
import java.util.List;

/** Countries for dropdowns and for checking a country code. Used by CountryController, StudentService and PersonAddressService. */
public interface CountryService {

    /**
     * Active countries, Cambodia first, then by English name.
     *
     * @param q               filters by English or Khmer name, nationality or exact code; null or empty = all
     * @param withNationality true leaves out territories nobody is a national of (for the Nationality list)
     * @return the countries
     */
    List<CountryResponse> list(String q, boolean withNationality);

    /**
     * The active country with this code, for saving a nationality or an address country.
     *
     * @param code ISO code, any case (" th " → TH); null or empty = Cambodia (KH)
     * @return the country
     * @throws com.ums.common.exception.BusinessException error.country.unknown when no active country has the code
     */
    Country require(String code);
}
