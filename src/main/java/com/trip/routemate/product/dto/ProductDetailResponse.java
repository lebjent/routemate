package com.trip.routemate.product.dto;

import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;

import java.math.BigDecimal;
import java.util.List;

/**
 * 공개 상품 상세 화면에 필요한 설명, 이용 안내, 판매 옵션을 제공한다.
 *
 * 상품 공통 취소 정책과 옵션별 취소 정책은 각각 별도 필드로 유지한다.
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "상품 상세 설명·이용 안내·판매 옵션을 포함하는 응답 DTO")
public record ProductDetailResponse(
        Long productId,
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
        Long destinationId,
        String destinationName,
        Long countryId,
        String countryName,
        Long regionId,
        String regionName,
        List<OptionItem> options
) {
    /** 상품 상세에서 선택할 수 있는 판매 옵션이다. */
    public record OptionItem(
            Long optionId,
            String optionName,
            String optionDesc,
            BigDecimal price,
            String currency,
            String cancellationPolicy,
            String validityText,
            String confirmationType
    ) {
        /** 옵션 엔티티를 상품 상세 응답 항목으로 변환한다. */
        public static OptionItem from(TravelProductOption option) {
            return new OptionItem(option.getOptionId(), option.getOptionName(), option.getOptionDesc(), option.getPrice(),
                    option.getCurrency(), option.getCancellationPolicy(), option.getValidityText(), option.getConfirmationType());
        }
    }

    /** 상품 엔티티와 판매 옵션을 공개 상세 응답으로 변환한다. */
    public static ProductDetailResponse from(TravelProduct product, List<TravelProductOption> options) {
        var destination = product.getDestination();
        return new ProductDetailResponse(
                product.getProductId(), product.getProductName(), product.getProductSummary(), product.getProductType(),
                product.getProviderName(), product.getProductDesc(), product.getImageUrl(), product.getDetailImageUrl(),
                product.getCourseText(), product.getIncludedText(), product.getExcludedText(), product.getUsageGuideText(),
                product.getNoticeText(), product.getCancellationPolicyText(), product.getFaqText(), product.getMeetingTime(),
                product.getMeetingPlace(), product.getBookingUrl(), destination.getDestId(), destination.getDestName(),
                destination.getCountry().getCountryId(), destination.getCountry().getCountryName(),
                destination.getRegion().getRegionId(), destination.getRegion().getRegionName(),
                options.stream().map(OptionItem::from).toList()
        );
    }
}
