package com.trip.routemate.admin.service;

import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.plan.domain.TravelPlan;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import com.trip.routemate.user.repository.UserMstrRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock private UserMstrRepository userMstrRepository;
    @Mock private TravelPlanRepository travelPlanRepository;
    @Mock private DestinationRepository destinationRepository;

    @InjectMocks private AdminDashboardService adminDashboardService;

    @Test
    void getDashboard_returnsCurrentSummaryAndPlanLists() {
        var popularPlan = plan(1L, "인기 일정", 25L);
        var recentPlan = plan(2L, "최근 일정", 3L);
        when(userMstrRepository.countByUserRoleAndDelYn("USER", "N")).thenReturn(12L);
        when(userMstrRepository.countByUserRoleAndUserStatCdAndDelYn("USER", "ACTIVE", "N")).thenReturn(10L);
        when(travelPlanRepository.count()).thenReturn(8L);
        when(travelPlanRepository.countByIsPublic("Y")).thenReturn(6L);
        when(destinationRepository.count()).thenReturn(14L);
        when(travelPlanRepository.getTotalViewCount()).thenReturn(120L);
        when(travelPlanRepository.findTop5ByIsPublicOrderByViewCountDescPlanIdDesc("Y"))
                .thenReturn(List.of(popularPlan));
        when(travelPlanRepository.findTop5ByOrderByCreateDtDesc()).thenReturn(List.of(recentPlan));

        var result = adminDashboardService.getDashboard();

        assertThat(result.summary().totalUsers()).isEqualTo(12L);
        assertThat(result.summary().activeUsers()).isEqualTo(10L);
        assertThat(result.summary().totalPlans()).isEqualTo(8L);
        assertThat(result.summary().publicPlans()).isEqualTo(6L);
        assertThat(result.summary().totalDestinations()).isEqualTo(14L);
        assertThat(result.summary().totalViews()).isEqualTo(120L);
        assertThat(result.popularPlans()).extracting("title").containsExactly("인기 일정");
        assertThat(result.recentPlans()).extracting("title").containsExactly("최근 일정");
    }

    private TravelPlan plan(Long id, String title, Long viewCount) {
        return TravelPlan.builder()
                .planId(id)
                .userNicknm("여행자")
                .title(title)
                .isPublic("Y")
                .viewCount(viewCount)
                .build();
    }
}
