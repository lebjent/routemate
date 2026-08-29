package com.trip.routemate.admin.dto;

import com.trip.routemate.plan.domain.TravelPlan;
import com.trip.routemate.product.domain.ProductOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "관리자 대시보드의 회원·여행지·상품·주문 요약 지표 DTO")
/** 관리자 대시보드에 표시하는 운영 요약과 최근 활동 데이터다. */
public record AdminDashboardResponse(
        Summary summary,
        List<ProductTypeItem> productTypes,
        List<OrderItem> recentOrders,
        List<PlanItem> popularPlans,
        List<PlanItem> recentPlans
) {
    public record Summary(
            long totalUsers,
            long activeUsers,
            long totalPlans,
            long publicPlans,
            long totalDestinations,
            long totalViews,
            long totalProducts,
            long activeProducts,
            long totalOptions,
            long activeOptions,
            long totalOrders,
            long paidOrders,
            long pendingPayments,
            BigDecimal paidRevenue
    ) {
    }

    public record ProductTypeItem(String productType, long totalCount, long activeCount) {
    }

    public record OrderItem(
            Long orderId,
            String orderNo,
            String productName,
            String optionName,
            String destinationName,
            int quantity,
            BigDecimal totalPrice,
            String currency,
            LocalDate useDate,
            String orderStatus,
            String paymentStatus,
            LocalDateTime createDt
    ) {
        public static OrderItem from(ProductOrder order) {
            return new OrderItem(
                    order.getOrderId(), order.getOrderNo(), order.getProductName(), order.getOptionName(),
                    order.getDestinationName(), order.getQuantity(), order.getTotalPrice(), order.getCurrency(),
                    order.getUseDate(), order.getOrderStatus(), order.getPaymentStatus(), order.getCreateDt()
            );
        }
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
