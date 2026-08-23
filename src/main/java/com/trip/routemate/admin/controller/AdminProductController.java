package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminProductRequest;
import com.trip.routemate.admin.dto.AdminProductResponse;
import com.trip.routemate.admin.dto.AdminProductApprovalRequest;
import com.trip.routemate.admin.dto.AdminProductApprovalHistoryResponse;
import com.trip.routemate.admin.service.AdminProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@Tag(name = "Admin Products", description = "관리자용 여행 옵션상품 및 상품 옵션 관리 API")
public class AdminProductController {
    private final AdminProductService productService;

    @GetMapping
    @Operation(summary = "관리자 상품 목록 조회", description = "여행지와 판매 상태를 기준으로 전체 상품, 판매 상품, 미판매 상품을 조회합니다.")
    public ResponseEntity<AdminProductResponse> getProducts(
                                                            @Parameter(description = "여행지 ID") @RequestParam(required = false) Long destinationId,
                                                            @Parameter(description = "판매 상태 필터: ALL, Y, N", example = "ALL") @RequestParam(defaultValue = "ALL") String useYn) {
        return ResponseEntity.ok(productService.getProducts(destinationId, useYn));
    }

    @PostMapping
    @Operation(summary = "상품 등록", description = "상품 기본 정보, 상세 설명, 이미지, 이용 안내와 하위 판매 옵션을 함께 등록합니다.")
    public ResponseEntity<AdminProductResponse.Item> create(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.status(201).body(productService.create(request));
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "상품 정보와 하위 옵션을 수정합니다. 관리자 또는 허용된 파트너 권한이 필요합니다.")
    public ResponseEntity<AdminProductResponse.Item> update(@PathVariable Long productId,
                                                             @Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(productService.update(productId, request));
    }

    @GetMapping("/approvals")
    public ResponseEntity<AdminProductResponse> getApprovalProducts(@RequestParam(defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(productService.getApprovalProducts(status));
    }

    @PatchMapping("/{productId}/approval")
    public ResponseEntity<AdminProductResponse.Item> review(@PathVariable Long productId,
                                                             @Valid @RequestBody AdminProductApprovalRequest request,
                                                             org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(productService.review(productId, request, authentication.getName()));
    }

    @GetMapping("/{productId}/approval-history")
    public ResponseEntity<java.util.List<AdminProductApprovalHistoryResponse>> history(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.approvalHistory(productId));
    }
}
