package com.trip.routemate.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 계획의 하루를 구성하는 요청이다.
 *
 * @param dayNumber 여행 시작일 기준 일차 번호
 * @param planDate 실제 여행 날짜
 * @param regions 그날 방문할 지역과 세부 일정
 */
public record TravelDayRequest(
        @NotNull Integer dayNumber,
        @NotNull LocalDate planDate,
        @NotNull List<@Valid TravelDayRegionRequest> regions
) {
}
