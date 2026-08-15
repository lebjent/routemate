package com.trip.routemate.destination.dto;

import com.trip.routemate.destination.domain.Country;

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
