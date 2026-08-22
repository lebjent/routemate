package com.trip.routemate.user.dto;

import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "로그인 사용자와 관리자 권한·메뉴 정보를 담는 응답 DTO")
public record UserLoginResponse(
        Long userId,
        String userEmail,
        String userNicknm,
        String userRole,
        List<String> permissions,
        List<String> menuCodes
) {
}
