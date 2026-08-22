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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "관리자 운영 현황 요약 API")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @Operation(summary = "관리자 대시보드 조회", description = "회원, 여행지, 상품, 파트너사와 주문 관련 운영 지표를 조회합니다.")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }
}
