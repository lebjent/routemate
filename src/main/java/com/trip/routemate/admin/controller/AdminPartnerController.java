package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminPartnerRequest;
import com.trip.routemate.admin.dto.AdminPartnerResponse;
import com.trip.routemate.admin.service.AdminPartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * 파트너사와 대표 계정을 관리하는 관리자 API다.
 *
 * 파트너사의 사업자 정보와 대표 계정은 함께 생성된다. 비밀번호 암호화, 중복 검증, 상태
 * 변경 규칙은 {@link AdminPartnerService}에 있으며 컨트롤러는 이를 직접 처리하지 않는다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/partners")
@Tag(name = "Admin Partners", description = "관리자용 파트너사 등록·승인·상품 공급자 관리 API")
public class AdminPartnerController {
    private final AdminPartnerService partnerService;

    /**
     * 파트너사 목록을 검색하고 상품 운영 건수를 함께 조회한다.
     *
     * @param query 파트너사명 또는 등록 코드 검색어
     * @param status 운영 상태 필터. 없으면 모든 상태를 조회한다.
     * @return 파트너사별 기본 정보와 상품 집계
     */
    @GetMapping
    @Operation(summary = "파트너사 목록 조회", description = "파트너사명, 사업자 코드와 승인 상태를 기준으로 파트너사를 조회합니다.")
    public AdminPartnerResponse getPartners(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return partnerService.getPartners(query, status);
    }

    /**
     * 파트너사와 대표 계정을 생성한다.
     *
     * 등록 코드는 서버에서 자동 생성하며, 대표 계정은 파트너 포털의 최초 관리자가 된다.
     *
     * @param request 사업자·계약·담당자·대표 계정 정보
     * @return 생성된 파트너사 정보와 HTTP 201
     */
    @PostMapping
    @Operation(summary = "파트너사 등록", description = "파트너사 기본 정보, 담당자, 수수료율과 계약 기간을 등록합니다.")
    public ResponseEntity<AdminPartnerResponse.Item> create(@Valid @RequestBody AdminPartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerService.create(request));
    }

    /**
     * 파트너사 기본 정보와 운영 상태를 수정한다.
     *
     * @param partnerId 수정할 파트너사 식별자
     * @param request 변경할 파트너사 및 대표 계정 정보
     * @return 수정된 파트너사 정보
     */
    @PatchMapping("/{partnerId}")
    @Operation(summary = "파트너사 수정 및 승인 상태 변경", description = "파트너사 정보와 ACTIVE, PENDING, SUSPENDED 등의 운영 상태를 변경합니다.")
    public AdminPartnerResponse.Item update(@PathVariable Long partnerId, @Valid @RequestBody AdminPartnerRequest request) {
        return partnerService.update(partnerId, request);
    }
}
