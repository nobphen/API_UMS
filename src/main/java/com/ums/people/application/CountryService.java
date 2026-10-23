package com.ums.people.application;

import com.ums.people.api.dto.CountryResponse;
import com.ums.people.domain.Country;
import java.util.List;

public interface CountryService {

    /** Cambodia first, then by English name. withNationality leaves out territories nobody is a national of. */
    List<CountryResponse> list(String q, boolean withNationality);

    /** The active country with this code; empty means Cambodia. Fails with a clear message for an unknown code. */
    Country require(String code);
}
