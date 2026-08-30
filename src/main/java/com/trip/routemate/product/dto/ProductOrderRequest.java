package com.trip.routemate.product.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 옵션 상품 예약을 생성할 때 사용하는 요청이다.
 *
 * @param productId 예약할 상품 식별자
 * @param items 구매할 판매 옵션별 수량
 * @param useDate 실제 상품 이용일
 * @param quantity 예약 수량
 * @param buyerName 예약자 이름
 * @param buyerEmail 예약자 이메일
 * @param buyerPhone 예약자 연락처
 */
public record ProductOrderRequest(
        @Schema(description = "상품 ID", example = "1001")
        @NotNull Long productId,
        @Schema(description = "선택한 상품 옵션과 수량", example = "[{\"optionId\":2001,\"quantity\":2},{\"optionId\":2002,\"quantity\":1}]")
        @NotEmpty @Size(max = 10) List<@Valid Item> items,
        @Schema(description = "상품 이용일. 오늘 이후 날짜", example = "2026-09-15")
        @NotNull @FutureOrPresent LocalDate useDate,
        @Schema(description = "구매자명", example = "홍길동")
        @NotBlank @Size(max = 50) String buyerName,
        @NotBlank @Email @Size(max = 100) String buyerEmail,
        @Size(max = 20) String buyerPhone
) {
    /** 한 판매 옵션의 구매 수량이다. */
    public record Item(
            @NotNull Long optionId,
            @NotNull @Min(1) @Max(10) Integer quantity
    ) {
    }
}
