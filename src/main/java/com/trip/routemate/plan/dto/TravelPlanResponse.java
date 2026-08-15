package com.trip.routemate.plan.dto;

import com.trip.routemate.plan.domain.TravelPlan;

import java.time.LocalDateTime;
import java.time.LocalDate;

public record TravelPlanResponse(
        Long planId,
        String title,
        String description,
        String imageUrl,
        String userNicknm,
        Integer spotCount,
        Integer likeCount,
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
                plan.getIsPublic(),
                plan.getTravelStartDate(),
                plan.getTravelEndDate(),
                plan.getCreateDt(),
                plan.getMdfyDt()
        );
    }
}
