package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** 관리자가 국가 마스터를 등록하거나 수정할 때 전달하는 값이다. */
public record AdminCountryRequest(@NotBlank String countryName, @NotBlank String countryCode, String countryStatCd) {
}
