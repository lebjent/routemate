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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/partners")
@Tag(name = "Admin Partners", description = "관리자용 파트너사 등록·승인·상품 공급자 관리 API")
public class AdminPartnerController {
    private final AdminPartnerService partnerService;

    @GetMapping
    @Operation(summary = "파트너사 목록 조회", description = "파트너사명, 사업자 코드와 승인 상태를 기준으로 파트너사를 조회합니다.")
    public AdminPartnerResponse getPartners(@RequestParam(required = false) String query, @RequestParam(required = false) String status) {
        return partnerService.getPartners(query, status);
    }

    @PostMapping
    @Operation(summary = "파트너사 등록", description = "파트너사 기본 정보, 담당자, 수수료율과 계약 기간을 등록합니다.")
    public ResponseEntity<AdminPartnerResponse.Item> create(@Valid @RequestBody AdminPartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerService.create(request));
    }

    @PatchMapping("/{partnerId}")
    @Operation(summary = "파트너사 수정 및 승인 상태 변경", description = "파트너사 정보와 ACTIVE, PENDING, SUSPENDED 등의 운영 상태를 변경합니다.")
    public AdminPartnerResponse.Item update(@PathVariable Long partnerId, @Valid @RequestBody AdminPartnerRequest request) {
        return partnerService.update(partnerId, request);
    }
}
