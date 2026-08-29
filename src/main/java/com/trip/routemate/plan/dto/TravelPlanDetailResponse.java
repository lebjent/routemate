package com.trip.routemate.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 여행 계획의 일차별 일정과 준비물을 포함하는 상세 응답이다.
 *
 * 목록 화면용 요약 응답과 달리 모든 방문 지역, 세부 일정, 교통편, 연결 예약을 포함한다.
 */
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
    /** 여행 계획의 하루와 그날 방문하는 지역들이다. */
    public record Day(
            Integer dayNumber,
            LocalDate planDate,
            List<Region> regions
    ) {
    }

    /** 일차에 방문하는 한 지역과 그 지역의 세부 일정이다. */
    public record Region(
            String countryCode,
            String countryName,
            String regionCode,
            String regionName,
            String note,
            List<Schedule> schedules
    ) {
    }

    /** 시간대별 방문 일정, 교통편, 연결 예약 정보다. */
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

    /** 여행 준비물과 필수 여부다. */
    public record PackingItem(
            String item,
            boolean required
    ) {
    }
}
