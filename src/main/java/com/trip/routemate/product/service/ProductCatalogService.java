package com.trip.routemate.product.service;

import com.trip.routemate.product.dto.ProductDetailResponse;
import com.trip.routemate.product.dto.ProductSummaryResponse;
import com.trip.routemate.product.repository.TravelProductOptionRepository;
import com.trip.routemate.product.repository.TravelProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCatalogService {
    private final TravelProductRepository productRepository;
    private final TravelProductOptionRepository optionRepository;

    public List<ProductSummaryResponse> getProducts(String productType, String query) {
        var products = productRepository.findPublicProducts();
        if (products.isEmpty()) return List.of();

        var optionsByProduct = optionRepository
                .findAllByProductInAndUseYnOrderByProductProductIdAscSortOrderAscOptionIdAsc(products, "Y")
                .stream()
                .collect(Collectors.groupingBy(option -> option.getProduct().getProductId()));
        var normalizedType = normalize(productType).toUpperCase(Locale.ROOT);
        var normalizedQuery = normalize(query).toLowerCase(Locale.ROOT);

        return products.stream()
                .filter(product -> normalizedType.isBlank() || normalizedType.equals(product.getProductType()))
                .filter(product -> matchesQuery(product.getProductName(), product.getProductSummary(),
                        product.getDestination().getDestName(), product.getDestination().getCountry().getCountryName(),
                        product.getDestination().getRegion().getRegionName(), normalizedQuery))
                .filter(product -> !optionsByProduct.getOrDefault(product.getProductId(), List.of()).isEmpty())
                .map(product -> ProductSummaryResponse.from(product, optionsByProduct.get(product.getProductId())))
                .toList();
    }

    public ProductDetailResponse getProduct(Long productId) {
        var product = productRepository.findWithDestinationByProductId(productId)
                .filter(found -> "Y".equals(found.getUseYn()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "판매 중인 옵션상품을 찾을 수 없습니다."));
        var options = optionRepository.findAllByProductAndUseYnOrderBySortOrderAscOptionIdAsc(product, "Y");
        return ProductDetailResponse.from(product, options);
    }

    private boolean matchesQuery(String productName, String summary, String destination, String country, String region, String query) {
        if (query.isBlank()) return true;
        return Stream.of(productName, summary, destination, country, region)
                .filter(value -> value != null)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(query));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
