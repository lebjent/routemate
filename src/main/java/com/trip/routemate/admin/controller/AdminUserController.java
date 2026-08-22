package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminUserListResponse;
import com.trip.routemate.admin.dto.AdminUserStatusUpdateRequest;
import com.trip.routemate.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users", description = "일반 회원 목록 및 이용 상태 관리 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "회원 목록 조회")
    public ResponseEntity<AdminUserListResponse> getUsers(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        return ResponseEntity.ok(adminUserService.getUsers(query, status));
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "회원 상태 변경")
    public ResponseEntity<AdminUserListResponse.UserItem> updateUserStatus(
            Authentication authentication,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateUserStatus(
                authentication.getName(),
                userId,
                request.userStatCd()
        ));
    }
}
