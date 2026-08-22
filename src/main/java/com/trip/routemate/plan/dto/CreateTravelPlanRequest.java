package com.trip.routemate.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "여행 일정과 일차별 방문지·교통·준비물을 생성·수정하는 요청 DTO")
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
