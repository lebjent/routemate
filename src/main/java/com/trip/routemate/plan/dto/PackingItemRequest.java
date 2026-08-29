package com.trip.routemate.plan.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 여행 계획 전체에 추가하는 준비물 항목이다.
 *
 * @param item 준비물 이름
 * @param required 필수 준비물 여부
 */
public record PackingItemRequest(
        @NotBlank String item,
        Boolean required
) {
}
