package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminUserStatusUpdateRequest(
        @NotBlank(message = "변경할 회원 상태를 입력해 주세요.")
        String userStatCd
) {
}
