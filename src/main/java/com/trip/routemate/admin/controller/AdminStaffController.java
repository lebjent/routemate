package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminStaffListResponse;
import com.trip.routemate.admin.dto.AdminStaffCreateRequest;
import com.trip.routemate.admin.dto.AdminStaffRoleUpdateRequest;
import com.trip.routemate.admin.dto.AdminUserStatusUpdateRequest;
import com.trip.routemate.admin.service.AdminStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 직원 계정과 역할을 관리하는 관리자 API다.
 *
 * 역할 및 상태 변경은 현재 로그인한 관리자 자신의 계정에는 적용할 수 없다. 이 보호 규칙은
 * 서비스 계층에서 강제해 실수로 운영 권한을 잃는 상황을 방지한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/staff")
@Tag(name = "Admin Staff", description = "관리자 직원 계정과 역할·상태 관리 API")
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    /**
     * 관리자 직원 목록과 역할별 요약을 조회한다.
     *
     * @param query 이름 또는 이메일 검색어
     * @param status 계정 상태 필터. {@code ALL}이면 상태를 제한하지 않는다.
     * @param role 역할 필터. {@code ALL}이면 역할을 제한하지 않는다.
     * @return 직원 목록과 상태·역할 집계
     */
    @GetMapping
    @Operation(summary = "직원 목록 조회")
    public ResponseEntity<AdminStaffListResponse> getStaff(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "ALL") String role
    ) {
        return ResponseEntity.ok(adminStaffService.getStaff(query, status, role));
    }

    /**
     * 새 관리자 직원 계정을 생성한다.
     *
     * @param request 로그인 정보, 이름, 역할, 초기 상태
     * @return 생성된 직원 정보와 HTTP 201
     */
    @PostMapping
    @Operation(summary = "직원 계정 등록")
    public ResponseEntity<AdminStaffListResponse.StaffItem> createStaff(
            @Valid @RequestBody AdminStaffCreateRequest request
    ) {
        return ResponseEntity.status(201).body(adminStaffService.createStaff(request));
    }

    /**
     * 다른 관리자 직원의 역할을 변경한다.
     *
     * @param authentication 현재 로그인한 관리자 정보
     * @param userId 역할을 변경할 직원 식별자
     * @param request 새 역할 코드
     * @return 변경된 직원 정보
     */
    @PatchMapping("/{userId}/role")
    @Operation(summary = "직원 역할 변경")
    public ResponseEntity<AdminStaffListResponse.StaffItem> updateRole(
            Authentication authentication,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminStaffRoleUpdateRequest request
    ) {
        return ResponseEntity.ok(adminStaffService.updateRole(authentication.getName(), userId, request.userRole()));
    }

    /**
     * 다른 관리자 직원의 사용 상태를 변경한다.
     *
     * @param authentication 현재 로그인한 관리자 정보
     * @param userId 상태를 변경할 직원 식별자
     * @param request 새 계정 상태
     * @return 변경된 직원 정보
     */
    @PatchMapping("/{userId}/status")
    @Operation(summary = "직원 상태 변경")
    public ResponseEntity<AdminStaffListResponse.StaffItem> updateStatus(
            Authentication authentication,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(adminStaffService.updateStatus(authentication.getName(), userId, request.userStatCd()));
    }
}
