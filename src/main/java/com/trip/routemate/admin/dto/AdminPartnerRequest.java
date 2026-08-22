package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminPartnerRequest(
        @Schema(description = "파트너사 내부 식별 코드", example = "PARTNER-JP-001")
        @NotBlank @Size(max = 30) String partnerCode,
        @Schema(description = "파트너사명", example = "도쿄 트래블 컴퍼니")
        @NotBlank @Size(max = 120) String partnerName,
        @Size(max = 30) String businessNumber,
        @Size(max = 50) String representativeName,
        @Size(max = 50) String managerName,
        @Email @Size(max = 100) String managerEmail,
        @Size(max = 30) String managerPhone,
        @Size(max = 500) String websiteUrl,
        @Schema(description = "판매 수수료율(%)", example = "15.00")
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal commissionRate,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        @Schema(description = "파트너사 상태: PENDING, ACTIVE, SUSPENDED", example = "ACTIVE")
        @NotBlank String partnerStatus,
        @Size(max = 1000) String memo
) {
}
