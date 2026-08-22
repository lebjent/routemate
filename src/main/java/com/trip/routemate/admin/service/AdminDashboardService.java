package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminDashboardResponse;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import com.trip.routemate.product.repository.ProductOrderRepository;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
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
    private final TravelProductRepository travelProductRepository;
    private final TravelProductOptionRepository travelProductOptionRepository;
    private final ProductOrderRepository productOrderRepository;

    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public AdminDashboardResponse getDashboard() {
        var totalViews = travelPlanRepository.getTotalViewCount();
        var summary = new AdminDashboardResponse.Summary(
                userMstrRepository.countByUserRoleAndDelYn("USER", "N"),
                userMstrRepository.countByUserRoleAndUserStatCdAndDelYn("USER", "ACTIVE", "N"),
                travelPlanRepository.count(),
                travelPlanRepository.countByIsPublic("Y"),
                destinationRepository.count(),
                totalViews == null ? 0L : totalViews,
                travelProductRepository.count(),
                travelProductRepository.countByUseYn("Y"),
                travelProductOptionRepository.count(),
                travelProductOptionRepository.countByUseYn("Y"),
                productOrderRepository.count(),
                productOrderRepository.countByPaymentStatus("PAID"),
                productOrderRepository.countByPaymentStatus("PENDING"),
                productOrderRepository.getPaidRevenue()
        );

        var productTypes = travelProductRepository.findProductTypeStats().stream()
                .map(item -> new AdminDashboardResponse.ProductTypeItem(
                        item.getProductType(), item.getTotalCount(), item.getActiveCount()))
                .toList();
        var recentOrders = productOrderRepository.findTop5ByOrderByCreateDtDescOrderIdDesc().stream()
                .map(AdminDashboardResponse.OrderItem::from)
                .toList();

        var popularPlans = travelPlanRepository.findTop5ByIsPublicOrderByViewCountDescPlanIdDesc("Y")
                .stream()
                .map(AdminDashboardResponse.PlanItem::from)
                .toList();
        var recentPlans = travelPlanRepository.findTop5ByOrderByCreateDtDesc()
                .stream()
                .map(AdminDashboardResponse.PlanItem::from)
                .toList();

        return new AdminDashboardResponse(summary, productTypes, recentOrders, popularPlans, recentPlans);
    }
}
