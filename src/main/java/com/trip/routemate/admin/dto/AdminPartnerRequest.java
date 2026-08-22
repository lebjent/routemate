package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminPartnerRequest(
        @NotBlank @Size(max = 30) String partnerCode,
        @NotBlank @Size(max = 120) String partnerName,
        @Size(max = 30) String businessNumber,
        @Size(max = 50) String representativeName,
        @Size(max = 50) String managerName,
        @Email @Size(max = 100) String managerEmail,
        @Size(max = 30) String managerPhone,
        @Size(max = 500) String websiteUrl,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal commissionRate,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        @NotBlank String partnerStatus,
        @Size(max = 1000) String memo
) {
}
