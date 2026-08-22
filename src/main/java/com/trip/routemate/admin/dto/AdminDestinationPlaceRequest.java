package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminDestinationPlaceRequest(
        @NotBlank String destName,
        String destDesc,
        @NotNull Long countryId,
        @NotNull Long regionId,
        @NotNull PlaceCategory category,
        String imageUrl,
        @NotNull Double mapLat,
        @NotNull Double mapLng
) {
}
