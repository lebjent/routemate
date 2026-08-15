package com.trip.routemate.plan.dto;

public record TravelScheduleRequest(
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
