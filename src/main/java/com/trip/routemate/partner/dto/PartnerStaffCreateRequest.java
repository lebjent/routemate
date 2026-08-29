package com.trip.routemate.partner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 파트너 대표가 직원 계정을 등록할 때 전달하는 정보다. */
public record PartnerStaffCreateRequest(
        @NotBlank @Email @Size(max = 100) String loginId,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 50) String name
) {
}
