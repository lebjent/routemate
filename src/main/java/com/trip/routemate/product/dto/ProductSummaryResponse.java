package com.trip.routemate.product.dto;

import com.trip.routemate.product.domain.TravelProduct;
import com.trip.routemate.product.domain.TravelProductOption;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

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
    public static ProductSummaryResponse from(TravelProduct product, List<TravelProductOption> options) {
        var minimumOption = options.stream()
                .min(Comparator.comparing(TravelProductOption::getPrice))
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
