package com.trip.routemate.product.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ProductOrderRequest(
        @NotNull Long productId,
        @NotNull Long optionId,
        @NotNull @FutureOrPresent LocalDate useDate,
        @NotNull @Min(1) @Max(10) Integer quantity,
        @NotBlank @Size(max = 50) String buyerName,
        @NotBlank @Email @Size(max = 100) String buyerEmail,
        @Size(max = 20) String buyerPhone
) {
}
