package com.trip.routemate.product.controller;

import com.trip.routemate.product.dto.ProductOrderRequest;
import com.trip.routemate.product.dto.ProductOrderResponse;
import com.trip.routemate.product.service.ProductOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-orders")
@Tag(name = "Product Orders", description = "로그인 사용자의 여행 옵션상품 주문 API")
public class ProductOrderController {
    private final ProductOrderService productOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "옵션상품 주문 생성", description = "선택한 상품 옵션과 이용일, 구매자 정보를 저장합니다. 로그인 세션이 필요합니다.")
    public ProductOrderResponse createOrder(Authentication authentication, @Valid @RequestBody ProductOrderRequest request) {
        return productOrderService.createOrder(resolveUserEmail(authentication), request);
    }

    @GetMapping("/my")
    @Operation(summary = "내 상품 주문 목록 조회", description = "현재 로그인한 사용자의 주문 내역을 최신순으로 조회합니다.")
    public List<ProductOrderResponse> getMyOrders(Authentication authentication) {
        return productOrderService.getMyOrders(resolveUserEmail(authentication));
    }

    @GetMapping("/my/schedule-candidates")
    @Operation(summary = "일정에 추가할 예약 상품 조회", description = "선택한 일차의 날짜 및 국가·지역과 일치하는 로그인 사용자의 유효 예약 상품을 조회합니다.")
    public List<ProductOrderResponse> getMyScheduleCandidates(
            Authentication authentication,
            @RequestParam String countryCode,
            @RequestParam String regionCode,
            @RequestParam LocalDate useDate
    ) {
        return productOrderService.getMyScheduleCandidates(resolveUserEmail(authentication), countryCode, regionCode, useDate);
    }

    private String resolveUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
