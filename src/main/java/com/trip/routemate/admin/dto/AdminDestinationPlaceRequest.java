package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 관리자가 여행지 정보를 등록하거나 수정할 때 전달하는 값이다. */
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
