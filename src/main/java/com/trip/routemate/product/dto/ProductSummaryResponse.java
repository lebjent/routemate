package com.trip.routemate.product.dto;

import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 공개 상품 목록 카드에 표시할 최소 정보를 제공하는 응답이다.
 *
 * 표시 가격은 판매 중인 옵션 중 최저가이며, 옵션이 없는 상품은 목록에서 제외된다.
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "공개 상품 카드에 표시되는 요약 정보 DTO")
public record ProductSummaryResponse(
        Long productId,
        String productName,
        String productSummary,
        String productType,
        String providerName,
        String imageUrl,
        Long destinationId,
        String destinationName,
        Long countryId,
        String countryName,
        Long regionId,
        String regionName,
        BigDecimal minimumPrice,
        String currency,
        int optionCount
) {
    /** 상품과 판매 옵션을 목록 카드용 요약 정보로 변환한다. */
    public static ProductSummaryResponse from(TravelProduct product, List<TravelProductOption> options) {
        var minimumOption = options.stream()
                .min(Comparator.comparing(option -> option.getPrice()))
                .orElseThrow();
        var destination = product.getDestination();
        return new ProductSummaryResponse(
                product.getProductId(), product.getProductName(), product.getProductSummary(), product.getProductType(),
                product.getProviderName(), product.getImageUrl(), destination.getDestId(), destination.getDestName(),
                destination.getCountry().getCountryId(), destination.getCountry().getCountryName(),
                destination.getRegion().getRegionId(), destination.getRegion().getRegionName(),
                minimumOption.getPrice(), minimumOption.getCurrency(), options.size()
        );
    }
}
