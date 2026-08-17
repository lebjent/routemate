package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminCountryRequest(@NotBlank String countryName, @NotBlank String countryCode, String countryStatCd) {
}
