package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminRegionRequest(@NotBlank String regionName, @NotBlank String regionCode, Integer sortOrder, String regionStatCd) {
}
