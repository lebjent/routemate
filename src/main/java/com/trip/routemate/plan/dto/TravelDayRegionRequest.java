package com.trip.routemate.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 특정 여행 일차에 방문할 국가·지역과 세부 일정 목록이다.
 *
 * @param countryCode 방문 국가 코드
 * @param regionCode 방문 지역 코드
 * @param note 해당 지역 방문에 대한 메모
 * @param schedules 시간대별 세부 일정 목록
 */
public record TravelDayRegionRequest(
        @NotBlank String countryCode,
        @NotBlank String regionCode,
        String note,
        @NotNull List<@Valid TravelScheduleRequest> schedules
) {
}
