package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminProductOptionRequest(
        @NotBlank @Size(max = 150) String optionName,
        @Size(max = 500) String optionDesc,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Size(max = 500) String cancellationPolicy,
        @Size(max = 200) String validityText,
        @NotBlank @Size(max = 20) String confirmationType,
        String useYn,
        Integer sortOrder
) {}
