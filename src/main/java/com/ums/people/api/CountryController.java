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

/** The Country (addresses) and Nationality (persons) dropdowns, for any logged-in user. */
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    /** All countries, Cambodia first. withNationality=true: only countries people are nationals of, for the Nationality list. */
    @GetMapping
    public ApiResponse<List<CountryResponse>> list(@RequestParam(required = false) String q,
                                                   @RequestParam(defaultValue = "false") boolean withNationality) {
        return ApiResponse.ok(countryService.list(q, withNationality));
    }
}
