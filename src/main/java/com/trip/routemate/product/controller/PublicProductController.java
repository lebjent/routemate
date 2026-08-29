package com.trip.routemate.product.controller;

import com.trip.routemate.product.dto.ProductDetailResponse;
import com.trip.routemate.product.dto.ProductSummaryResponse;
import com.trip.routemate.product.service.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 승인된 판매 상품을 로그인 없이 탐색할 수 있도록 제공하는 공개 카탈로그 API다.
 *
 * 판매 중이며 활성 파트너사에 속한 상품만 노출한다. 심사 대기·거절·보류 상품은 이 API에서
 * 절대 반환하지 않는다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/products")
@Tag(name = "Public Product", description = "승인된 여행 옵션상품의 공개 카탈로그 API")
public class PublicProductController {
    private final ProductCatalogService productCatalogService;

    /**
     * 공개 판매 상품을 유형과 검색어 조건으로 조회한다.
     *
     * @param productType 선택 조건인 상품 유형
     * @param query 상품명, 요약, 제공사에 적용할 검색어
     * @return 구매 가능한 상품 요약 목록
     */
    @GetMapping
    @Operation(summary = "공개 상품 목록 조회", description = "판매 중이고 파트너사가 활성 상태인 옵션상품을 유형과 검색어로 조회합니다.")
    public List<ProductSummaryResponse> getProducts(
            @Parameter(description = "상품 유형 필터", example = "TICKET")
            @RequestParam(required = false) String productType,
            @Parameter(description = "상품명·요약·제공사 검색어", example = "유니버설 스튜디오")
            @RequestParam(required = false) String query
    ) {
        return productCatalogService.getProducts(productType, query);
    }

    /**
     * 공개 판매 상품의 상세 설명과 구매 가능한 옵션을 조회한다.
     *
     * @param productId 조회할 상품 식별자
     * @return 상품 상세와 옵션 목록
     */
    @GetMapping("/{productId}")
    @Operation(summary = "공개 상품 상세 조회", description = "상품 설명, 상세 이미지, 이용 안내와 구매 가능한 옵션을 조회합니다.")
    public ProductDetailResponse getProduct(@PathVariable Long productId) {
        return productCatalogService.getProduct(productId);
    }
}
