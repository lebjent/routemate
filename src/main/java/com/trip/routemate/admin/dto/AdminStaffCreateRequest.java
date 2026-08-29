package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 관리자 직원 계정을 생성할 때 사용하는 요청이다. */
public record AdminStaffCreateRequest(
        @NotBlank @Email String userEmail,
        @NotBlank @Size(min = 8, max = 100) String userPwd,
        @NotBlank @Size(max = 50) String userNicknm,
        @NotBlank String userRole
) {
}
