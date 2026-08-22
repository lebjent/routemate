package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductRequest(
        @NotNull Long destinationId,
        Long partnerId,
        @NotBlank @Size(max = 150) String productName,
        @Size(max = 300) String productSummary,
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
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        String useYn,
        Integer sortOrder,
        List<@jakarta.validation.Valid AdminProductOptionRequest> options
) {
}
