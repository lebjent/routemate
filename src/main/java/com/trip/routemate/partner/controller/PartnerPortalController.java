package com.trip.routemate.partner.controller;

import com.trip.routemate.partner.dto.PartnerDashboardResponse;
import com.trip.routemate.partner.dto.PartnerProductRequest;
import com.trip.routemate.partner.dto.PartnerProductResponse;
import com.trip.routemate.partner.dto.PartnerProductImageUploadResponse;
import com.trip.routemate.partner.service.PartnerProductImageStorageService;
import com.trip.routemate.partner.service.PartnerPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 파트너 포털의 대시보드와 옵션 상품 관리 API를 제공한다.
 *
 * 모든 요청의 파트너사 범위는 URL이나 요청 본문이 아니라 로그인 인증 정보에서 결정한다.
 * 따라서 다른 파트너사의 상품을 조회하거나 변경할 수 없다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner")
public class PartnerPortalController {
    private final PartnerPortalService service;
    private final PartnerProductImageStorageService productImageStorageService;

    /**
     * 로그인한 파트너사의 상품·판매 현황을 조회한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @return 상품 상태, 판매 수량, 최근 등록 상품을 포함한 대시보드 정보
     */
    @GetMapping("/dashboard")
    public PartnerDashboardResponse dashboard(Authentication authentication) {
        return service.dashboard(authentication);
    }

    /**
     * 로그인한 파트너사가 관리할 수 있는 옵션 상품을 조회한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @return 파트너사 소유 상품과 옵션 목록
     */
    @GetMapping("/products")
    public List<PartnerProductResponse> products(Authentication authentication) {
        return service.products(authentication);
    }

    /**
     * 상품 등록 화면에서 선택한 대표 이미지를 개발용 로컬 폴더에 저장한다.
     *
     * 반환된 URL은 상품 저장 요청의 {@code imageUrl} 값으로 전달되어 DB에 보관된다.
     */
    @PostMapping(value = "/products/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerProductImageUploadResponse uploadProductImage(Authentication authentication,
                                                                 @RequestPart("file") MultipartFile file) {
        return productImageStorageService.store(authentication, file);
    }

    /**
     * 상품 등록 시 선택할 수 있는 여행지 목록을 조회한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @return 여행지 식별자, 이름, 국가, 지역 정보
     */
    @GetMapping("/destinations/places")
    public List<PartnerPortalService.PlaceItem> places(Authentication authentication) {
        return service.places(authentication);
    }

    /**
     * 파트너사 명의의 옵션 상품을 등록한다.
     *
     * 새 상품은 운영 심사를 위해 승인 대기 상태로 저장된다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @param request 상품과 하위 판매 옵션 정보
     * @return 생성된 상품 정보와 HTTP 201
     */
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerProductResponse create(Authentication authentication,
                                         @Valid @RequestBody PartnerProductRequest request) {
        return service.create(authentication, request);
    }

    /**
     * 로그인한 파트너사 소유 상품만 수정한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @param productId 수정할 상품 식별자
     * @param request 변경할 상품과 옵션 정보
     * @return 수정된 상품 정보
     */
    @PatchMapping("/products/{productId}")
    public PartnerProductResponse update(Authentication authentication,
                                         @PathVariable Long productId,
                                         @Valid @RequestBody PartnerProductRequest request) {
        return service.update(authentication, productId, request);
    }
}
