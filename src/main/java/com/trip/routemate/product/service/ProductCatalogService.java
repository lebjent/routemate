package com.trip.routemate.product.service;

import com.trip.routemate.product.dto.ProductDetailResponse;
import com.trip.routemate.product.dto.ProductSummaryResponse;
import com.trip.routemate.product.domain.TravelProduct;
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

/**
 * 공개 상품 카탈로그의 노출 조건과 조회 응답 변환을 담당한다.
 *
 * 판매 상태, 심사 상태, 파트너사 상태를 모두 만족하는 상품만 공개한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCatalogService {
    private final TravelProductRepository productRepository;
    private final TravelProductOptionRepository optionRepository;

    /**
     * 공개 가능한 상품을 유형과 통합 검색어로 필터링한다.
     *
     * @param productType 선택 조건인 상품 유형
     * @param query 상품·여행지·제공사에 적용할 검색어
     * @return 판매 가능한 상품 요약 목록
     */
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

    /** 공개 가능한 상품 하나의 상세 설명과 판매 옵션을 조회한다. */
    public ProductDetailResponse getProduct(Long productId) {
        var product = productRepository.findWithDestinationByProductId(productId)
                .filter(this::isSellable)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "판매 중인 옵션상품을 찾을 수 없습니다."));
        var options = optionRepository.findAllByProductAndUseYnOrderBySortOrderAscOptionIdAsc(product, "Y");
        return ProductDetailResponse.from(product, options);
    }

    /** 목록과 동일한 판매 정책을 상세 조회에도 적용한다. */
    private boolean isSellable(TravelProduct product) {
        return "Y".equals(product.getUseYn())
                && "APPROVED".equals(product.getApprovalStatus())
                && (product.getPartner() == null || "ACTIVE".equals(product.getPartner().getPartnerStatus()));
    }

    /** 상품명, 요약, 여행지, 국가, 지역 중 하나라도 검색어를 포함하는지 확인한다. */
    private boolean matchesQuery(String productName, String summary, String destination, String country, String region, String query) {
        if (query.isBlank()) return true;
        return Stream.of(productName, summary, destination, country, region)
                .filter(value -> value != null)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(query));
    }

    /** null 값을 빈 문자열로 바꾸고 앞뒤 공백을 제거한다. */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
