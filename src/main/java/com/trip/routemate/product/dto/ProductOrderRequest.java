package com.trip.routemate.product.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ProductOrderRequest(
        @Schema(description = "상품 ID", example = "1001")
        @NotNull Long productId,
        @Schema(description = "선택한 상품 옵션 ID", example = "2001")
        @NotNull Long optionId,
        @Schema(description = "상품 이용일. 오늘 이후 날짜", example = "2026-09-15")
        @NotNull @FutureOrPresent LocalDate useDate,
        @Schema(description = "구매 수량(1~10)", example = "2")
        @NotNull @Min(1) @Max(10) Integer quantity,
        @Schema(description = "구매자명", example = "홍길동")
        @NotBlank @Size(max = 50) String buyerName,
        @NotBlank @Email @Size(max = 100) String buyerEmail,
        @Size(max = 20) String buyerPhone
) {
}
