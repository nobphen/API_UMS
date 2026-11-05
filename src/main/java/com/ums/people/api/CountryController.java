package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.people.api.dto.CountryResponse;
import com.ums.people.application.CountryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Countries (ISO 3166-1: KH, TH, VN …) for the Country dropdown of an address and the Nationality dropdown of a
 * person. Read only, open to every logged-in user; the list is loaded by a migration (V5__seed_countries.sql).
 */
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    /**
     * All active countries, Cambodia first, then by English name. q matches the English or Khmer name, the
     * nationality or the exact code. withNationality=true leaves out territories nobody is a national of
     * (for the Nationality list).
     * <pre>GET /api/v1/countries
     * GET /api/v1/countries?q=thai
     * GET /api/v1/countries?withNationality=true</pre>
     */
    @GetMapping
    public ApiResponse<List<CountryResponse>> list(@RequestParam(required = false) String q,
                                                   @RequestParam(defaultValue = "false") boolean withNationality) {
        return ApiResponse.ok(countryService.list(q, withNationality));
    }
}
