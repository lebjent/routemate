package com.trip.routemate.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** 파트너 포털 상품 등록·수정 요청입니다. 파트너 식별자는 로그인 세션에서만 결정합니다. */
public record PartnerProductRequest(
        @Schema(description = "상품이 판매되는 장소 ID", example = "101") @NotNull Long destinationId,
        @NotBlank @Size(max = 150) String productName,
        @Size(max = 300) String productSummary,
        @NotBlank @Size(max = 30) String productType,
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
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        String useYn,
        Integer sortOrder,
        List<@Valid PartnerProductOptionRequest> options
) {
}
