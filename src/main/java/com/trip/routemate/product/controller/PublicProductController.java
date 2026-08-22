package com.trip.routemate.product.controller;

import com.trip.routemate.product.dto.ProductDetailResponse;
import com.trip.routemate.product.dto.ProductSummaryResponse;
import com.trip.routemate.product.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/products")
public class PublicProductController {
    private final ProductCatalogService productCatalogService;

    @GetMapping
    public List<ProductSummaryResponse> getProducts(
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String query
    ) {
        return productCatalogService.getProducts(productType, query);
    }

    @GetMapping("/{productId}")
    public ProductDetailResponse getProduct(@PathVariable Long productId) {
        return productCatalogService.getProduct(productId);
    }
}
