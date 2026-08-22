package com.trip.routemate.admin.dto;

import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "관리자 상품 목록과 상품별 옵션을 담는 응답 DTO")
public record AdminProductResponse(List<Item> products) {
    public record Item(Long productId, Long destinationId, String destinationName, String countryName, String regionName,
                       Long partnerId, String partnerCode, String partnerName, String productName, String productSummary, String productType, String providerName, String productDesc,
                       String imageUrl, String detailImageUrl, String courseText, String includedText, String excludedText,
                       String usageGuideText, String noticeText, String cancellationPolicyText, String faqText,
                       String meetingTime, String meetingPlace, String bookingUrl, BigDecimal price, String currency, String useYn,
                       Integer sortOrder, String registrationSource, String approvalStatus, String approvalMemo,
                       LocalDateTime createDt, List<OptionItem> options) {
        public static Item from(TravelProduct product, List<TravelProductOption> productOptions) {
            var destination = product.getDestination();
            var partner = product.getPartner();
            return new Item(product.getProductId(), destination.getDestId(), destination.getDestName(),
                    destination.getCountry().getCountryName(), destination.getRegion().getRegionName(),
                    partner == null ? null : partner.getPartnerId(), partner == null ? null : partner.getPartnerCode(), partner == null ? null : partner.getPartnerName(),
                    product.getProductName(), product.getProductSummary(), product.getProductType(), product.getProviderName(), product.getProductDesc(),
                    product.getImageUrl(), product.getDetailImageUrl(), product.getCourseText(), product.getIncludedText(), product.getExcludedText(),
                    product.getUsageGuideText(), product.getNoticeText(), product.getCancellationPolicyText(), product.getFaqText(),
                    product.getMeetingTime(), product.getMeetingPlace(), product.getBookingUrl(), product.getPrice(), product.getCurrency(),
                    product.getUseYn(), product.getSortOrder(), product.getRegistrationSource(), product.getApprovalStatus(), product.getApprovalMemo(),
                    product.getCreateDt(), productOptions.stream().map(OptionItem::from).toList());
        }
    }
    public record OptionItem(Long optionId, String optionName, String optionDesc, BigDecimal price, String currency,
                             String cancellationPolicy, String validityText, String confirmationType, String useYn, Integer sortOrder) {
        static OptionItem from(TravelProductOption option) { return new OptionItem(option.getOptionId(), option.getOptionName(), option.getOptionDesc(), option.getPrice(), option.getCurrency(), option.getCancellationPolicy(), option.getValidityText(), option.getConfirmationType(), option.getUseYn(), option.getSortOrder()); }
    }
}
