package com.trip.routemate.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 계획 전체 구조를 생성하거나 교체할 때 사용하는 요청이다.
 *
 * @param title 여행 계획 제목
 * @param description 여행 계획 설명
 * @param imageUrl 대표 이미지 주소
 * @param isPublic 공개 여부. {@code N}만 비공개로 처리한다.
 * @param travelStartDate 여행 시작일
 * @param travelEndDate 여행 종료일
 * @param days 날짜별 방문 지역과 세부 일정
 * @param packingItems 여행 전체 준비물 목록
 */
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
