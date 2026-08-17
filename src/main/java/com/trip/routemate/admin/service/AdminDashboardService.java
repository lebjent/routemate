package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminDashboardResponse;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserMstrRepository userMstrRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final DestinationRepository destinationRepository;

    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public AdminDashboardResponse getDashboard() {
        var totalViews = travelPlanRepository.getTotalViewCount();
        var summary = new AdminDashboardResponse.Summary(
                userMstrRepository.countByUserRoleAndDelYn("USER", "N"),
                userMstrRepository.countByUserRoleAndUserStatCdAndDelYn("USER", "ACTIVE", "N"),
                travelPlanRepository.count(),
                travelPlanRepository.countByIsPublic("Y"),
                destinationRepository.count(),
                totalViews == null ? 0L : totalViews
        );

        var popularPlans = travelPlanRepository.findTop5ByIsPublicOrderByViewCountDescPlanIdDesc("Y")
                .stream()
                .map(AdminDashboardResponse.PlanItem::from)
                .toList();
        var recentPlans = travelPlanRepository.findTop5ByOrderByCreateDtDesc()
                .stream()
                .map(AdminDashboardResponse.PlanItem::from)
                .toList();

        return new AdminDashboardResponse(summary, popularPlans, recentPlans);
    }
}
