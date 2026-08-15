package com.trip.routemate.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TravelDayRegionRequest(
        @NotBlank String countryCode,
        @NotBlank String regionCode,
        String note,
        @NotNull List<@Valid TravelScheduleRequest> schedules
) {
}
