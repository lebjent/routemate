package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** 일반 회원의 사용 상태를 변경하는 관리자 요청이다. */
public record AdminUserStatusUpdateRequest(
        @NotBlank(message = "변경할 회원 상태를 입력해 주세요.")
        String userStatCd
) {
}
