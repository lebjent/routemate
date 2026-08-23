package com.trip.routemate.partner.dto;

import java.math.BigDecimal;
import java.util.List;

public record PartnerDashboardResponse(
        String partnerName,
        long totalProducts,
        long activeProducts,
        long totalOrders,
        BigDecimal paidRevenue,
        List<ProductItem> popularProducts
) {
    public record ProductItem(Long productId, String productName) { }
}
