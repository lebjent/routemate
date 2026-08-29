package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 관리자가 상품 심사 결과와 사유를 저장할 때 전달하는 요청이다. */
public record AdminProductApprovalRequest(
        @NotBlank String decisionStatus,
        @Size(max = 500) String reason
) { }
