package com.trip.routemate.product.dto;

import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;

import java.math.BigDecimal;
import java.util.List;

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
        public static OptionItem from(TravelProductOption option) {
            return new OptionItem(option.getOptionId(), option.getOptionName(), option.getOptionDesc(), option.getPrice(),
                    option.getCurrency(), option.getCancellationPolicy(), option.getValidityText(), option.getConfirmationType());
        }
    }

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
