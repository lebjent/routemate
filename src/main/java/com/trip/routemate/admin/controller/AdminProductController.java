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

/**
 * 전체 옵션 상품과 심사 프로세스를 관리하는 관리자 API다.
 *
 * 파트너사가 등록한 상품은 {@code PENDING} 상태에서 이 API의 심사를 거쳐야 한다.
 * 승인, 거절, 보류 결과와 사유는 상품의 현재 상태뿐 아니라 승인 이력에도 보관한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@Tag(name = "Admin Products", description = "관리자용 여행 옵션상품 및 상품 옵션 관리 API")
public class AdminProductController {
    private final AdminProductService productService;

    /**
     * 관리자 등록 상품을 여행지와 판매 상태 조건으로 조회한다.
     *
     * @param destinationId 선택 조건인 여행지 식별자
     * @param useYn 판매 상태. {@code ALL}, {@code Y}, {@code N} 중 하나다.
     * @return 조건에 맞는 상품과 하위 옵션 목록
     */
    @GetMapping
    @Operation(summary = "관리자 상품 목록 조회", description = "여행지와 판매 상태를 기준으로 전체 상품, 판매 상품, 미판매 상품을 조회합니다.")
    public ResponseEntity<AdminProductResponse> getProducts(
                                                            @Parameter(description = "여행지 ID") @RequestParam(required = false) Long destinationId,
                                                            @Parameter(description = "판매 상태 필터: ALL, Y, N", example = "ALL") @RequestParam(defaultValue = "ALL") String useYn) {
        return ResponseEntity.ok(productService.getProducts(destinationId, useYn));
    }

    /**
     * 관리자 권한으로 상품과 하위 옵션을 함께 등록한다.
     *
     * @param request 상품 설명, 이용 정보, 판매 조건, 옵션 목록
     * @return 생성된 상품 정보와 HTTP 201
     */
    @PostMapping
    @Operation(summary = "상품 등록", description = "상품 기본 정보, 상세 설명, 이미지, 이용 안내와 하위 판매 옵션을 함께 등록합니다.")
    public ResponseEntity<AdminProductResponse.Item> create(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.status(201).body(productService.create(request));
    }

    /**
     * 상품의 기본 정보와 하위 옵션 구성을 수정한다.
     *
     * @param productId 수정할 상품 식별자
     * @param request 변경할 상품과 옵션 정보
     * @return 수정된 상품 정보
     */
    @PatchMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "상품 정보와 하위 옵션을 수정합니다. 관리자 또는 허용된 파트너 권한이 필요합니다.")
    public ResponseEntity<AdminProductResponse.Item> update(@PathVariable Long productId,
                                                             @Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.ok(productService.update(productId, request));
    }

    /**
     * 파트너사가 제출한 상품을 심사 상태별로 조회한다.
     *
     * @param status 조회할 심사 상태. 기본값은 {@code PENDING}이다.
     * @return 심사 대상 상품 목록
     */
    @GetMapping("/approvals")
    public ResponseEntity<AdminProductResponse> getApprovalProducts(@RequestParam(defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(productService.getApprovalProducts(status));
    }

    /**
     * 상품 심사 결과를 승인, 거절, 보류 중 하나로 결정한다.
     *
     * 현재 인증 사용자를 심사자로 기록하며, 거절·보류는 운영자가 입력한 사유를 이력에 남긴다.
     *
     * @param productId 심사할 상품 식별자
     * @param request 결정 상태와 사유
     * @param authentication 현재 로그인한 관리자 인증 정보
     * @return 심사 결과가 반영된 상품 정보
     */
    @PatchMapping("/{productId}/approval")
    public ResponseEntity<AdminProductResponse.Item> review(@PathVariable Long productId,
                                                             @Valid @RequestBody AdminProductApprovalRequest request,
                                                             org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(productService.review(productId, request, authentication.getName()));
    }

    /**
     * 상품의 승인·거절·보류 처리 이력을 최신 순으로 조회한다.
     *
     * @param productId 이력을 조회할 상품 식별자
     * @return 심사 상태, 사유, 처리자, 처리 시각 목록
     */
    @GetMapping("/{productId}/approval-history")
    public ResponseEntity<java.util.List<AdminProductApprovalHistoryResponse>> history(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.approvalHistory(productId));
    }
}
