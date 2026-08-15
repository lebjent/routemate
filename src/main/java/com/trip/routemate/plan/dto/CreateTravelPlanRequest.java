package com.trip.routemate.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateTravelPlanRequest(
        @NotBlank String title,
        String description,
        String imageUrl,
        String isPublic,
        @NotNull LocalDate travelStartDate,
        @NotNull LocalDate travelEndDate,
        @NotNull List<TravelDayRequest> days,
        List<PackingItemRequest> packingItems
) {
}
