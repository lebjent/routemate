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

/**
 * 일반 회원의 계정 상태를 조회하고 변경하는 관리자 API다.
 *
 * 관리자 역할 계정은 이 API의 변경 대상이 아니다. 관리자 계정은 직원 관리 API에서 별도로
 * 다뤄 권한 관리와 일반 회원 관리를 분리한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users", description = "일반 회원 목록 및 이용 상태 관리 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 일반 회원 목록과 계정 상태 집계를 조회한다.
     *
     * @param query 이름, 닉네임 또는 이메일 검색어
     * @param status 계정 상태 필터. {@code ALL}이면 상태를 제한하지 않는다.
     * @return 회원 목록과 상태별 집계
     */
    @GetMapping
    @Operation(summary = "회원 목록 조회")
    public ResponseEntity<AdminUserListResponse> getUsers(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        return ResponseEntity.ok(adminUserService.getUsers(query, status));
    }

    /**
     * 일반 회원의 사용 상태를 변경한다.
     *
     * @param authentication 현재 로그인한 관리자 정보
     * @param userId 상태를 변경할 회원 식별자
     * @param request 새 계정 상태
     * @return 변경된 회원 정보
     */
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
