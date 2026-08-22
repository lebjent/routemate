package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductRequest(
        @Schema(description = "상품이 판매되는 장소 ID", example = "101")
        @NotNull Long destinationId,
        @Schema(description = "상품을 공급하는 파트너사 ID", example = "12")
        Long partnerId,
        @Schema(description = "상품명", example = "도쿄 디즈니랜드 1일권")
        @NotBlank @Size(max = 150) String productName,
        @Schema(description = "상품 목록에 표시할 짧은 설명", example = "QR코드로 바로 입장하는 공식 입장권")
        @Size(max = 300) String productSummary,
        @Schema(description = "상품 유형 코드", example = "TICKET")
        @NotBlank @Size(max = 30) String productType,
        @Size(max = 100) String providerName,
        String productDesc,
        @Size(max = 500) String imageUrl,
        @Size(max = 500) String detailImageUrl,
        String courseText,
        String includedText,
        String excludedText,
        String usageGuideText,
        String noticeText,
        String cancellationPolicyText,
        String faqText,
        @Size(max = 100) String meetingTime,
        @Size(max = 300) String meetingPlace,
        @Size(max = 1000) String bookingUrl,
        @Schema(description = "기본 판매 가격", example = "89000")
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @Schema(description = "통화 코드", example = "KRW")
        @NotBlank @Size(min = 3, max = 3) String currency,
        String useYn,
        Integer sortOrder,
        List<@jakarta.validation.Valid AdminProductOptionRequest> options
) {
}
