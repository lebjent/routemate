package com.trip.routemate.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TravelPlanDetailResponse(
        Long planId,
        String title,
        String description,
        String imageUrl,
        String userNicknm,
        Integer spotCount,
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
            String transportMemo
        ) {
    }

    public record PackingItem(
            String item,
            boolean required
    ) {
    }
}
