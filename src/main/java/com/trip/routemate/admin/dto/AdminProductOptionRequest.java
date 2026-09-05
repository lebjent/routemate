package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** 관리자가 옵션 상품의 판매 조건을 입력할 때 사용하는 요청이다. */
public record AdminProductOptionRequest(
        @NotBlank @Size(max = 150) String optionName,
        @Size(max = 500) String optionDesc,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Size(max = 500) String cancellationPolicy,
        @Size(max = 200) String validityText,
        @NotBlank @Size(max = 20) String confirmationType,
        String useYn,
        Integer sortOrder,
        Long optionId
) {
    public AdminProductOptionRequest(String optionName, String optionDesc, BigDecimal price, String currency,
                                     String cancellationPolicy, String validityText, String confirmationType,
                                     String useYn, Integer sortOrder) {
        this(optionName, optionDesc, price, currency, cancellationPolicy, validityText, confirmationType, useYn, sortOrder, null);
    }
}
