package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.domain.DestinationRecommendation;

import java.time.LocalDateTime;
import java.util.List;

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
