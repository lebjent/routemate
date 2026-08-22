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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/staff")
@Tag(name = "Admin Staff", description = "관리자 직원 계정과 역할·상태 관리 API")
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    @GetMapping
    @Operation(summary = "직원 목록 조회")
    public ResponseEntity<AdminStaffListResponse> getStaff(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "ALL") String role
    ) {
        return ResponseEntity.ok(adminStaffService.getStaff(query, status, role));
    }

    @PostMapping
    @Operation(summary = "직원 계정 등록")
    public ResponseEntity<AdminStaffListResponse.StaffItem> createStaff(
            @Valid @RequestBody AdminStaffCreateRequest request
    ) {
        return ResponseEntity.status(201).body(adminStaffService.createStaff(request));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "직원 역할 변경")
    public ResponseEntity<AdminStaffListResponse.StaffItem> updateRole(
            Authentication authentication,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminStaffRoleUpdateRequest request
    ) {
        return ResponseEntity.ok(adminStaffService.updateRole(authentication.getName(), userId, request.userRole()));
    }

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
