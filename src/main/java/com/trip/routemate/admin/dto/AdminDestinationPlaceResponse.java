package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.Destination;

import java.util.List;

public record AdminDestinationPlaceResponse(List<PlaceItem> places) {
    public record PlaceItem(Long destinationId, String destName, String destDesc, Long countryId, String countryName, Long regionId, String regionName, String category, String imageUrl, Double mapLat, Double mapLng, Integer likeCount) {
        public static PlaceItem from(Destination destination) {
            return new PlaceItem(destination.getDestId(), destination.getDestName(), destination.getDestDesc(), destination.getCountry().getCountryId(), destination.getCountry().getCountryName(), destination.getRegion().getRegionId(), destination.getRegion().getRegionName(), destination.getCategory(), destination.getImageUrl(), destination.getMapLat(), destination.getMapLng(), destination.getLikeCount());
        }
    }
}
