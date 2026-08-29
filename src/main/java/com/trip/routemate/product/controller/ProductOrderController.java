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

/**
 * 로그인 사용자의 옵션 상품 예약을 생성하고 조회하는 API다.
 *
 * 예약 소유자는 인증 정보로 결정된다. 일정에 연결할 수 있는 예약 후보도 같은 사용자 범위에서
 * 이용일과 여행지 조건을 모두 만족하는 예약만 반환한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-orders")
@Tag(name = "Product Orders", description = "로그인 사용자의 여행 옵션상품 주문 API")
public class ProductOrderController {
    private final ProductOrderService productOrderService;

    /**
     * 선택한 판매 옵션에 대한 예약을 생성한다.
     *
     * @param authentication 현재 로그인 사용자 인증 정보
     * @param request 옵션 식별자, 이용일, 예약자 정보, 수량
     * @return 생성된 예약 정보와 HTTP 201
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "옵션상품 주문 생성", description = "선택한 상품 옵션과 이용일, 구매자 정보를 저장합니다. 로그인 세션이 필요합니다.")
    public ProductOrderResponse createOrder(Authentication authentication, @Valid @RequestBody ProductOrderRequest request) {
        return productOrderService.createOrder(resolveUserEmail(authentication), request);
    }

    /**
     * 현재 사용자의 예약 내역을 최신순으로 조회한다.
     *
     * @param authentication 현재 로그인 사용자 인증 정보
     * @return 예약 목록
     */
    @GetMapping("/my")
    @Operation(summary = "내 상품 주문 목록 조회", description = "현재 로그인한 사용자의 주문 내역을 최신순으로 조회합니다.")
    public List<ProductOrderResponse> getMyOrders(Authentication authentication) {
        return productOrderService.getMyOrders(resolveUserEmail(authentication));
    }

    /**
     * 특정 여행 일차에 연결할 수 있는 유효 예약을 조회한다.
     *
     * @param authentication 현재 로그인 사용자 인증 정보
     * @param countryCode 여행 일정의 국가 코드
     * @param regionCode 여행 일정의 지역 코드
     * @param useDate 여행 일정 날짜
     * @return 여행지와 이용일이 일치하는 예약 목록
     */
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

    /** 인증 객체에서 로그인 이메일을 추출하고 미인증 요청은 거부한다. */
    private String resolveUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
