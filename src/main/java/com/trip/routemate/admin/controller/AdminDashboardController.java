package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminDashboardResponse;
import com.trip.routemate.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 운영 현황을 제공하는 HTTP 진입점이다.
 *
 * 화면에 필요한 집계와 최근 활동을 한 번에 반환하며, 집계 규칙과 데이터 조합은
 * {@link AdminDashboardService}에 둔다. 이 컨트롤러는 요청 경로와 HTTP 응답만 책임진다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "관리자 운영 현황 요약 API")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 관리자 대시보드를 조회한다.
     *
     * 회원·여행지·상품·파트너사·주문 집계와 최근 주문 및 일정을 함께 반환한다.
     * 권한 검증은 Spring Security의 관리자 경로 정책에서 수행한다.
     *
     * @return 대시보드에 표시할 운영 요약 정보
     */
    @GetMapping
    @Operation(summary = "관리자 대시보드 조회", description = "회원, 여행지, 상품, 파트너사와 주문 관련 운영 지표를 조회합니다.")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }
}
