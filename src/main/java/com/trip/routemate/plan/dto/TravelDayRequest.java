package com.trip.routemate.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record TravelDayRequest(
        @NotNull Integer dayNumber,
        @NotNull LocalDate planDate,
        @NotNull List<@Valid TravelDayRegionRequest> regions
) {
}
