package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** 국가에 속한 지역 마스터를 등록하거나 수정하는 요청이다. */
public record AdminRegionRequest(@NotBlank String regionName, @NotBlank String regionCode, Integer sortOrder, String regionStatCd) {
}
