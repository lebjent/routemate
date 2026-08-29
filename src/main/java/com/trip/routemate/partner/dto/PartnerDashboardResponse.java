package com.trip.routemate.partner.dto;

import java.math.BigDecimal;
import java.util.List;

/** 파트너 포털 첫 화면에 표시할 판매·상품 운영 지표다. */
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
