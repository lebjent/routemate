package com.trip.routemate.partner.controller;

import com.trip.routemate.partner.dto.PartnerStaffCreateRequest;
import com.trip.routemate.partner.dto.PartnerStaffResponse;
import com.trip.routemate.partner.service.PartnerStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 파트너사 대표가 자기 회사의 직원 계정을 관리하는 API다.
 *
 * 직원 목록, 생성, 상태 변경은 현재 인증 사용자의 파트너사 범위로 제한된다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner/staff")
public class PartnerStaffController {
    private final PartnerStaffService partnerStaffService;

    /**
     * 로그인한 파트너사에 소속된 직원 목록을 조회한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @return 파트너사 정보와 직원 목록
     */
    @GetMapping
    public PartnerStaffResponse getStaff(Authentication authentication) {
        return partnerStaffService.getStaff(authentication);
    }

    /**
     * 로그인한 파트너사에 새 직원 계정을 생성한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @param request 직원 로그인 정보와 역할
     * @return 생성된 직원 정보와 HTTP 201
     */
    @PostMapping
    public ResponseEntity<PartnerStaffResponse.Item> createStaff(Authentication authentication,
                                                                   @Valid @RequestBody PartnerStaffCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerStaffService.createStaff(authentication, request));
    }

    /**
     * 소속 직원의 사용 상태를 변경한다.
     *
     * @param authentication 현재 파트너 사용자 인증 정보
     * @param partnerUserId 상태를 변경할 파트너 직원 연결 식별자
     * @param request {@code status} 키에 새 상태를 담은 요청 본문
     * @return 변경된 직원 정보
     */
    @PatchMapping("/{partnerUserId}/status")
    public PartnerStaffResponse.Item updateStatus(Authentication authentication, @PathVariable Long partnerUserId,
                                                  @RequestBody java.util.Map<String, String> request) {
        return partnerStaffService.updateStatus(authentication, partnerUserId, request.get("status"));
    }
}
