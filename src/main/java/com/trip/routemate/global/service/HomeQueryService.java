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

/**
 * 홈 화면에 필요한 추천 여행지와 공개 일정 데이터를 조합한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeQueryService {

    private final DestinationRepository destinationRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final DestinationRecommendationRepository destinationRecommendationRepository;

    /** 홈 화면의 추천 콘텐츠를 조회한다. 활성 추천이 없으면 좋아요 순 여행지를 사용한다. */
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

    /** 홈 화면 응답이다.
     * @param destinations 노출할 여행지 목록
     * @param plans 인기 공개 일정 목록
     */
    public record HomeDataResponse(List<HomeDestinationResponse> destinations, List<TravelPlanResponse> plans) {
    }

    /** 홈 화면에 표시할 여행지 요약 정보다.
     * @param destId 여행지 식별자
     * @param destName 여행지명
     * @param destDesc 소개 문구
     * @param imageUrl 대표 이미지 주소
     * @param country 국가명
     * @param city 도시 또는 지역명
     * @param category 여행지 분류명
     * @param likeCount 좋아요 수
     */
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
        /** 여행지 엔티티를 홈 화면용 요약 정보로 변환한다. */
        private static HomeDestinationResponse from(com.trip.routemate.destination.domain.Destination destination) {
            return new HomeDestinationResponse(
                    destination.getDestId(), destination.getDestName(), destination.getDestDesc(), destination.getImageUrl(),
                    destination.getCountry().getCountryName(), destination.getRegion().getRegionName(),
                    destination.getCategory().getLabel(), destination.getLikeCount()
            );
        }
    }
}
