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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/products")
@Tag(name = "Public Product", description = "승인된 여행 옵션상품의 공개 카탈로그 API")
public class PublicProductController {
    private final ProductCatalogService productCatalogService;

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

    @GetMapping("/{productId}")
    @Operation(summary = "공개 상품 상세 조회", description = "상품 설명, 상세 이미지, 이용 안내와 구매 가능한 옵션을 조회합니다.")
    public ProductDetailResponse getProduct(@PathVariable Long productId) {
        return productCatalogService.getProduct(productId);
    }
}
