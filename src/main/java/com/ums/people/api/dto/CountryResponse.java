package com.ums.people.api.dto;

/** One row of the Country and Nationality dropdowns. */
public record CountryResponse(
        String code,              // KH, TH … send this back as countryCode / nationalityCode
        String nameEn,            // Cambodia
        String nameKh,            // កម្ពុជា
        String nationalityEn,     // Cambodian
        String nationalityKh) {   // ខ្មែរ
}
