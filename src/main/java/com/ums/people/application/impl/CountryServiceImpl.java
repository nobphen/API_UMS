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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Override
    public List<CountryResponse> list(String q, boolean withNationality) {
        String search = q == null ? "" : q.trim();   // "" matches every name
        return countryRepository.findForList(search, withNationality).stream()
                .map(countryMapper::toResponse)
                .toList();
    }

    @Override
    public Country require(String code) {
        String value = code == null || code.isBlank() ? Country.CAMBODIA : code.trim().toUpperCase();
        return countryRepository.findById(value)
                .filter(Country::isActive)
                .orElseThrow(() -> new BusinessException("error.country.unknown", value));
    }
}
