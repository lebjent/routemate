package com.trip.routemate.destination.dto;

import com.trip.routemate.destination.domain.Country;

/** 여행지 선택 화면에 제공하는 국가 정보다. */
public record CountryResponse(
        Long countryId,
        String countryName,
        String countryCode
) {
    public static CountryResponse from(Country country) {
        return new CountryResponse(
                country.getCountryId(),
                country.getCountryName(),
                country.getCountryCode()
        );
    }
}
