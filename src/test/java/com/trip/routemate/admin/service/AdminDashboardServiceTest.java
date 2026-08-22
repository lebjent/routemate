package com.trip.routemate.admin.service;

import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.plan.domain.TravelPlan;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import com.trip.routemate.product.repository.ProductOrderRepository;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import com.trip.routemate.user.repository.UserMstrRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock private UserMstrRepository userMstrRepository;
    @Mock private TravelPlanRepository travelPlanRepository;
    @Mock private DestinationRepository destinationRepository;
    @Mock private TravelProductRepository travelProductRepository;
    @Mock private TravelProductOptionRepository travelProductOptionRepository;
    @Mock private ProductOrderRepository productOrderRepository;

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
        when(travelProductRepository.count()).thenReturn(100L);
        when(travelProductRepository.countByUseYn("Y")).thenReturn(96L);
        when(travelProductOptionRepository.count()).thenReturn(300L);
        when(travelProductOptionRepository.countByUseYn("Y")).thenReturn(288L);
        when(productOrderRepository.count()).thenReturn(7L);
        when(productOrderRepository.countByPaymentStatus("PAID")).thenReturn(4L);
        when(productOrderRepository.countByPaymentStatus("PENDING")).thenReturn(2L);
        when(productOrderRepository.getPaidRevenue()).thenReturn(new BigDecimal("380000"));
        when(travelProductRepository.findProductTypeStats()).thenReturn(List.of());
        when(productOrderRepository.findTop5ByOrderByCreateDtDescOrderIdDesc()).thenReturn(List.of());
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
        assertThat(result.summary().totalProducts()).isEqualTo(100L);
        assertThat(result.summary().activeProducts()).isEqualTo(96L);
        assertThat(result.summary().totalOptions()).isEqualTo(300L);
        assertThat(result.summary().activeOptions()).isEqualTo(288L);
        assertThat(result.summary().totalOrders()).isEqualTo(7L);
        assertThat(result.summary().paidOrders()).isEqualTo(4L);
        assertThat(result.summary().pendingPayments()).isEqualTo(2L);
        assertThat(result.summary().paidRevenue()).isEqualByComparingTo("380000");
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
