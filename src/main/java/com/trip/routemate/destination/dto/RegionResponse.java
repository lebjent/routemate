package com.trip.routemate.destination.dto;

import com.trip.routemate.destination.domain.Region;

/** 지정 국가에서 선택 가능한 지역 정보다. */
public record RegionResponse(
        Long regionId,
        String regionName,
        String regionCode,
        Long countryId,
        String countryCode
) {
    public static RegionResponse from(Region region, Long countryId, String countryCode) {
        return new RegionResponse(
                region.getRegionId(),
                region.getRegionName(),
                region.getRegionCode(),
                countryId,
                countryCode
        );
    }
}
