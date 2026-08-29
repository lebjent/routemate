package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.domain.DestinationRecommendation;

import java.time.LocalDateTime;
import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "홈 화면 추천 콘텐츠와 추천 대상 장소 목록 응답 DTO")
/** 추천 목록과 추천 대상으로 선택할 수 있는 여행지를 함께 제공하는 응답이다. */
public record AdminRecommendationResponse(List<Item> recommendations, List<DestinationOption> destinations) {
    public record Item(Long recommendId, Long destinationId, Long countryId, Long regionId, String destinationName, String countryName, String regionName, String imageUrl, Integer likeCount, LocalDateTime displayStartDt, LocalDateTime displayEndDt, Integer sortOrder, String useYn) {
        public static Item from(DestinationRecommendation recommendation) {
            var destination = recommendation.getDestination();
            return new Item(recommendation.getRecommendId(), destination.getDestId(), destination.getCountry().getCountryId(), destination.getRegion().getRegionId(), destination.getDestName(), destination.getCountry().getCountryName(), destination.getRegion().getRegionName(), recommendation.getImageUrl() == null ? destination.getImageUrl() : recommendation.getImageUrl(), destination.getLikeCount(), recommendation.getDisplayStartDt(), recommendation.getDisplayEndDt(), recommendation.getSortOrder(), recommendation.getUseYn());
        }
    }

    public record DestinationOption(Long destinationId, String destinationName, Long countryId, String countryName, Long regionId, String regionName, String imageUrl) {
        public static DestinationOption from(Destination destination) {
            return new DestinationOption(destination.getDestId(), destination.getDestName(), destination.getCountry().getCountryId(), destination.getCountry().getCountryName(), destination.getRegion().getRegionId(), destination.getRegion().getRegionName(), destination.getImageUrl());
        }
    }
}
