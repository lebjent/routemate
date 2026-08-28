package com.trip.routemate.partner.dto;

import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 파트너 포털에서 관리할 수 있는 상품과 옵션 응답입니다. */
public record PartnerProductResponse(
        Long productId,
        Long destinationId,
        String destinationName,
        String countryName,
        String regionName,
        Long partnerId,
        String partnerCode,
        String partnerName,
        String productName,
        String productSummary,
        String productType,
        String providerName,
        String productDesc,
        String imageUrl,
        String detailImageUrl,
        String courseText,
        String includedText,
        String excludedText,
        String usageGuideText,
        String noticeText,
        String cancellationPolicyText,
        String faqText,
        String meetingTime,
        String meetingPlace,
        String bookingUrl,
        BigDecimal price,
        String currency,
        String useYn,
        Integer sortOrder,
        String registrationSource,
        String approvalStatus,
        String approvalMemo,
        LocalDateTime createDt,
        List<Option> options
) {
    public static PartnerProductResponse from(TravelProduct product, List<TravelProductOption> options) {
        var destination = product.getDestination();
        var partner = product.getPartner();
        return new PartnerProductResponse(
                product.getProductId(), destination.getDestId(), destination.getDestName(),
                destination.getCountry().getCountryName(), destination.getRegion().getRegionName(),
                partner == null ? null : partner.getPartnerId(), partner == null ? null : partner.getPartnerCode(), partner == null ? null : partner.getPartnerName(),
                product.getProductName(), product.getProductSummary(), product.getProductType(), product.getProviderName(), product.getProductDesc(),
                product.getImageUrl(), product.getDetailImageUrl(), product.getCourseText(), product.getIncludedText(),
                product.getExcludedText(), product.getUsageGuideText(), product.getNoticeText(), product.getCancellationPolicyText(),
                product.getFaqText(), product.getMeetingTime(), product.getMeetingPlace(), product.getBookingUrl(),
                product.getPrice(), product.getCurrency(), product.getUseYn(), product.getSortOrder(), product.getRegistrationSource(), product.getApprovalStatus(),
                product.getApprovalMemo(), product.getCreateDt(), options.stream().map(Option::from).toList()
        );
    }

    public record Option(
            Long optionId,
            String optionName,
            String optionDesc,
            BigDecimal price,
            String currency,
            String cancellationPolicy,
            String validityText,
            String confirmationType,
            String useYn,
            Integer sortOrder
    ) {
        private static Option from(TravelProductOption option) {
            return new Option(option.getOptionId(), option.getOptionName(), option.getOptionDesc(), option.getPrice(), option.getCurrency(),
                    option.getCancellationPolicy(), option.getValidityText(), option.getConfirmationType(), option.getUseYn(), option.getSortOrder());
        }
    }
}
