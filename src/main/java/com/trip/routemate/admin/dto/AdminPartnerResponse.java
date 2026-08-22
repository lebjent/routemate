package com.trip.routemate.admin.dto;

import com.trip.routemate.partner.domain.PartnerCompany;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminPartnerResponse(List<Item> partners) {
    public record Item(
            Long partnerId, String partnerCode, String partnerName, String businessNumber,
            String representativeName, String managerName, String managerEmail, String managerPhone,
            String websiteUrl, BigDecimal commissionRate, LocalDate contractStartDate, LocalDate contractEndDate,
            String partnerStatus, String memo, long totalProducts, long activeProducts, long pendingProducts,
            LocalDateTime createDt, LocalDateTime mdfyDt
    ) {
        public static Item from(PartnerCompany partner, ProductCount count) {
            return new Item(partner.getPartnerId(), partner.getPartnerCode(), partner.getPartnerName(), partner.getBusinessNumber(),
                    partner.getRepresentativeName(), partner.getManagerName(), partner.getManagerEmail(), partner.getManagerPhone(),
                    partner.getWebsiteUrl(), partner.getCommissionRate(), partner.getContractStartDate(), partner.getContractEndDate(),
                    partner.getPartnerStatus(), partner.getMemo(), count.totalProducts(), count.activeProducts(), count.pendingProducts(),
                    partner.getCreateDt(), partner.getMdfyDt());
        }
    }
    public record ProductCount(long totalProducts, long activeProducts, long pendingProducts) {
        public static ProductCount empty() { return new ProductCount(0, 0, 0); }
    }
}
