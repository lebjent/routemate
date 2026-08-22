package com.trip.routemate.plan.dto;

import com.trip.routemate.plan.domain.TravelPlan;

import java.time.LocalDateTime;
import java.time.LocalDate;

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
