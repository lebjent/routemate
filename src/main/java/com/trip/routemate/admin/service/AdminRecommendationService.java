package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminRecommendationRequest;
import com.trip.routemate.admin.dto.AdminRecommendationResponse;
import com.trip.routemate.destination.domain.DestinationRecommendation;
import com.trip.routemate.destination.repository.DestinationRecommendationRepository;
import com.trip.routemate.destination.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRecommendationService {
    private final DestinationRecommendationRepository recommendationRepository;
    private final DestinationRepository destinationRepository;

    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminRecommendationResponse getRecommendations() {
        var recommendations = recommendationRepository.findAllByOrderBySortOrderAscDisplayStartDtDesc().stream().map(AdminRecommendationResponse.Item::from).toList();
        var destinations = destinationRepository.findAllByOrderByLikeCountDesc().stream().map(AdminRecommendationResponse.DestinationOption::from).toList();
        return new AdminRecommendationResponse(recommendations, destinations);
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminRecommendationResponse.Item create(AdminRecommendationRequest request) {
        var destination = getDestination(request.countryId(), request.regionId());
        validatePeriod(request);
        var recommendation = recommendationRepository.save(DestinationRecommendation.builder()
                .destination(destination)
                .imageUrl(normalizeImageUrl(request.imageUrl()))
                .displayStartDt(request.displayStartDt())
                .displayEndDt(request.displayEndDt())
                .sortOrder(normalizeSortOrder(request.sortOrder()))
                .useYn(normalizeUseYn(request.useYn()))
                .build());
        return AdminRecommendationResponse.Item.from(recommendation);
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminRecommendationResponse.Item update(Long recommendId, AdminRecommendationRequest request) {
        var recommendation = recommendationRepository.findWithDestinationByRecommendId(recommendId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 여행지를 찾을 수 없습니다."));
        var destination = getDestination(request.countryId(), request.regionId());
        validatePeriod(request);
        recommendation.update(destination, normalizeImageUrl(request.imageUrl()), request.displayStartDt(), request.displayEndDt(), normalizeSortOrder(request.sortOrder()), normalizeUseYn(request.useYn()));
        return AdminRecommendationResponse.Item.from(recommendation);
    }

    private com.trip.routemate.destination.domain.Destination getDestination(Long countryId, Long regionId) { return destinationRepository.findTopByCountryCountryIdAndRegionRegionIdOrderByLikeCountDesc(countryId, regionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "선택한 국가·지역에 등록된 여행지가 없습니다.")); }
    private void validatePeriod(AdminRecommendationRequest request) { if (!request.displayStartDt().isBefore(request.displayEndDt())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "노출 종료일은 시작일보다 늦어야 합니다."); }
    private int normalizeSortOrder(Integer sortOrder) { return sortOrder == null || sortOrder < 1 ? 1 : sortOrder; }
    private String normalizeUseYn(String useYn) { return "N".equalsIgnoreCase(useYn) ? "N" : "Y"; }
    private String normalizeImageUrl(String imageUrl) { return imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim(); }
}
