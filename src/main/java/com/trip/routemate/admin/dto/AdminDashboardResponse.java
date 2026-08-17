package com.trip.routemate.admin.dto;

import com.trip.routemate.plan.domain.TravelPlan;

import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardResponse(
        Summary summary,
        List<PlanItem> popularPlans,
        List<PlanItem> recentPlans
) {
    public record Summary(
            long totalUsers,
            long activeUsers,
            long totalPlans,
            long publicPlans,
            long totalDestinations,
            long totalViews
    ) {
    }

    public record PlanItem(
            Long planId,
            String title,
            String userNicknm,
            String isPublic,
            long viewCount,
            LocalDateTime createDt
    ) {
        public static PlanItem from(TravelPlan plan) {
            return new PlanItem(
                    plan.getPlanId(),
                    plan.getTitle(),
                    plan.getUserNicknm(),
                    plan.getIsPublic(),
                    plan.getViewCount() == null ? 0L : plan.getViewCount(),
                    plan.getCreateDt()
            );
        }
    }
}
