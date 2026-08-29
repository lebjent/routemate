package com.trip.routemate.plan.dto;

import com.trip.routemate.plan.domain.TravelPlan;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 여행 계획 목록과 생성·수정 결과에 사용하는 요약 응답이다.
 *
 * 상세 일정은 포함하지 않으며, 필요한 경우 {@link TravelPlanDetailResponse}를 사용한다.
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "여행 계획 목록 또는 생성·수정 결과의 요약 응답 DTO")
public record TravelPlanResponse(
        Long planId,
        String title,
        String description,
        String imageUrl,
        String userNicknm,
        Integer spotCount,
        Integer likeCount,
        Long viewCount,
        String isPublic,
        LocalDate travelStartDate,
        LocalDate travelEndDate,
        LocalDateTime createDt,
        LocalDateTime mdfyDt
) {
    /** 여행 계획 엔티티를 목록용 요약 응답으로 변환한다. */
    public static TravelPlanResponse from(TravelPlan plan) {
        return new TravelPlanResponse(
                plan.getPlanId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getImageUrl(),
                plan.getUserNicknm(),
                plan.getSpotCount(),
                plan.getLikeCount(),
                plan.getViewCount(),
                plan.getIsPublic(),
                plan.getTravelStartDate(),
                plan.getTravelEndDate(),
                plan.getCreateDt(),
                plan.getMdfyDt()
        );
    }
}
