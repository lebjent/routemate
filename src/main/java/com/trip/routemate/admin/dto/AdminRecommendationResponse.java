package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.domain.DestinationRecommendation;

import java.time.LocalDateTime;
import java.util.List;

public record AdminRecommendationResponse(List<Item> recommendations, List<DestinationOption> destinations) {
    public record Item(Long recommendId, Long destinationId, String destinationName, String countryName, String regionName, String imageUrl, Integer likeCount, LocalDateTime displayStartDt, LocalDateTime displayEndDt, Integer sortOrder, String useYn) {
        public static Item from(DestinationRecommendation recommendation) {
            var destination = recommendation.getDestination();
            return new Item(recommendation.getRecommendId(), destination.getDestId(), destination.getDestName(), destination.getCountry().getCountryName(), destination.getRegion().getRegionName(), destination.getImageUrl(), destination.getLikeCount(), recommendation.getDisplayStartDt(), recommendation.getDisplayEndDt(), recommendation.getSortOrder(), recommendation.getUseYn());
        }
    }

    public record DestinationOption(Long destinationId, String destinationName, String countryName, String regionName, String imageUrl) {
        public static DestinationOption from(Destination destination) {
            return new DestinationOption(destination.getDestId(), destination.getDestName(), destination.getCountry().getCountryName(), destination.getRegion().getRegionName(), destination.getImageUrl());
        }
    }
}
