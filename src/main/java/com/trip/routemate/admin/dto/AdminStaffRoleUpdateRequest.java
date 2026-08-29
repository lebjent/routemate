package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** 관리자 직원에게 부여할 역할을 변경하는 요청이다. */
public record AdminStaffRoleUpdateRequest(@NotBlank String userRole) {
}
