package com.trip.routemate.global.service;

import com.trip.routemate.destination.repository.DestinationRecommendationRepository;
import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.plan.dto.TravelPlanResponse;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeQueryService {

    private final DestinationRepository destinationRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final DestinationRecommendationRepository destinationRecommendationRepository;

    @Cacheable("homeData")
    public HomeDataResponse getHomeData() {
        var recommendedDestinations = destinationRecommendationRepository.findActiveDestinations(LocalDateTime.now(), PageRequest.of(0, 5));
        var destinations = recommendedDestinations.isEmpty()
                ? destinationRepository.findTop5ByOrderByLikeCountDesc()
                : recommendedDestinations;
        return new HomeDataResponse(
                destinations.stream().map(HomeDestinationResponse::from).toList(),
                travelPlanRepository.findTop5ByIsPublicOrderByViewCountDescPlanIdDesc("Y").stream()
                        .map(TravelPlanResponse::from)
                        .toList()
        );
    }

    public record HomeDataResponse(List<HomeDestinationResponse> destinations, List<TravelPlanResponse> plans) {
    }

    public record HomeDestinationResponse(
            Long destId,
            String destName,
            String destDesc,
            String imageUrl,
            String country,
            String city,
            String category,
            Integer likeCount
    ) {
        private static HomeDestinationResponse from(com.trip.routemate.destination.domain.Destination destination) {
            return new HomeDestinationResponse(
                    destination.getDestId(), destination.getDestName(), destination.getDestDesc(), destination.getImageUrl(),
                    destination.getCountry().getCountryName(), destination.getRegion().getRegionName(),
                    destination.getCategory().getLabel(), destination.getLikeCount()
            );
        }
    }
}
