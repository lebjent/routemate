package com.trip.routemate.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "여행 계획의 전체 일정과 상세 구성 응답 DTO")
public record TravelPlanDetailResponse(
        Long planId,
        String title,
        String description,
        String imageUrl,
        String userNicknm,
        Integer spotCount,
        Long viewCount,
        String isPublic,
        LocalDate travelStartDate,
        LocalDate travelEndDate,
        LocalDateTime createDt,
        List<Day> days,
        List<PackingItem> packingItems
) {
    public record Day(
            Integer dayNumber,
            LocalDate planDate,
            List<Region> regions
    ) {
    }

    public record Region(
            String countryCode,
            String countryName,
            String regionCode,
            String regionName,
            String note,
            List<Schedule> schedules
    ) {
    }

    public record Schedule(
            String time,
            String title,
            String location,
            String memo,
            String transportType,
            String transportName,
            String departureTime,
            String arrivalTime,
            String transportMemo,
            Long productOrderId,
            String productOrderNo
        ) {
    }

    public record PackingItem(
            String item,
            boolean required
    ) {
    }
}
