package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.Country;
import com.trip.routemate.destination.domain.Region;

import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "국가 목록과 국가별 등록·활성 요약을 담는 관리자 응답 DTO")
/** 국가와 지역 관리 화면에 필요한 요약 및 목록 응답이다. */
public record AdminDestinationResponse(Summary summary, List<CountryItem> countries) {
    public record Summary(long totalCountries, long activeCountries, long inactiveCountries, long totalRegions) {}

    public record CountryItem(Long countryId, String countryName, String countryCode, String countryStatCd, long regionCount) {
        public static CountryItem from(Country country, long regionCount) {
            return new CountryItem(country.getCountryId(), country.getCountryName(), country.getCountryCode(), country.getCountryStatCd(), regionCount);
        }
    }

    public record RegionItem(Long regionId, String regionName, String regionCode, Integer sortOrder, String regionStatCd) {
        public static RegionItem from(Region region) {
            return new RegionItem(region.getRegionId(), region.getRegionName(), region.getRegionCode(), region.getSortOrder(), region.getRegionStatCd());
        }
    }
}
