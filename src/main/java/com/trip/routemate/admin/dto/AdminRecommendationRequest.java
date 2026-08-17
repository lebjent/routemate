package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AdminRecommendationRequest(
        @NotNull Long destinationId,
        @NotNull LocalDateTime displayStartDt,
        @NotNull LocalDateTime displayEndDt,
        Integer sortOrder,
        String useYn
) {
}
