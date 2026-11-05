package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.people.api.dto.CountryResponse;
import com.ums.people.application.CountryService;
import com.ums.people.application.mapper.CountryMapper;
import com.ums.people.domain.Country;
import com.ums.people.infrastructure.CountryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link CountryService}. Countries are only read here (the list comes from a migration), so the whole class
 * is readOnly = true.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;   // entity → response (MapStruct)

    @Override
    public List<CountryResponse> list(String q, boolean withNationality) {
        String search = q == null ? "" : q.trim();   // "" matches every name
        return countryRepository.findForList(search, withNationality).stream()
                .map(countryMapper::toResponse)   // entity → response
                .toList();
    }

    @Override
    public Country require(String code) {

        // 1. Empty means Cambodia; otherwise the same style for everyone: " th " → "TH"
        String value = code == null || code.isBlank() ? Country.CAMBODIA : code.trim().toUpperCase();

        // 2. The country must exist and be active, or the caller gets a clear message instead of a database error
        return countryRepository.findById(value)
                .filter(Country::isActive)
                .orElseThrow(() -> new BusinessException("error.country.unknown", value));
    }
}
