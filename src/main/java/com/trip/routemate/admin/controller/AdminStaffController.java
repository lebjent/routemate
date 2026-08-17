package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminStaffListResponse;
import com.trip.routemate.admin.dto.AdminStaffCreateRequest;
import com.trip.routemate.admin.dto.AdminStaffRoleUpdateRequest;
import com.trip.routemate.admin.dto.AdminUserStatusUpdateRequest;
import com.trip.routemate.admin.service.AdminStaffService;
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
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    @GetMapping
    public ResponseEntity<AdminStaffListResponse> getStaff(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "ALL") String role
    ) {
        return ResponseEntity.ok(adminStaffService.getStaff(query, status, role));
    }

    @PostMapping
    public ResponseEntity<AdminStaffListResponse.StaffItem> createStaff(
            @Valid @RequestBody AdminStaffCreateRequest request
    ) {
        return ResponseEntity.status(201).body(adminStaffService.createStaff(request));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<AdminStaffListResponse.StaffItem> updateRole(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody AdminStaffRoleUpdateRequest request
    ) {
        return ResponseEntity.ok(adminStaffService.updateRole(authentication.getName(), userId, request.userRole()));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminStaffListResponse.StaffItem> updateStatus(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(adminStaffService.updateStatus(authentication.getName(), userId, request.userStatCd()));
    }
}
