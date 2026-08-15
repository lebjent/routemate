package com.trip.routemate.plan.dto;

import jakarta.validation.constraints.NotBlank;

public record PackingItemRequest(
        @NotBlank String item,
        Boolean required
) {
}
