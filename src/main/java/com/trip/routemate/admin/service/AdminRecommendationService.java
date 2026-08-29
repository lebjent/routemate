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

import java.util.Objects;

/**
 * 홈 화면 여행지 추천의 등록·수정·조회 규칙을 처리한다.
 *
 * 추천은 여행지 자체가 아니라 노출 기간과 순서를 가진 별도 설정이다. 이 서비스는 국가와
 * 지역 입력값으로 실제 여행지를 결정하고, 잘못된 기간이나 존재하지 않는 대상은 요청 오류로
 * 처리한다. 모든 기능에는 {@code DESTINATION_MANAGE} 권한이 필요하다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRecommendationService {
    private final DestinationRecommendationRepository recommendationRepository;
    private final DestinationRepository destinationRepository;

    /**
     * 모든 추천 설정과 화면에서 선택할 수 있는 여행지를 조회한다.
     *
     * 추천 목록은 표시 순서 오름차순, 시작 시각 내림차순으로 반환한다. 아직 기간이 시작되지
     * 않았거나 종료된 설정도 관리 화면에서 수정할 수 있도록 포함한다.
     *
     * @return 추천 설정 목록과 여행지 선택 목록
     */
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminRecommendationResponse getRecommendations() {
        var recommendations = recommendationRepository.findAllByOrderBySortOrderAscDisplayStartDtDesc().stream().map(AdminRecommendationResponse.Item::from).toList();
        var destinations = destinationRepository.findAllByOrderByLikeCountDesc().stream().map(AdminRecommendationResponse.DestinationOption::from).toList();
        return new AdminRecommendationResponse(recommendations, destinations);
    }

    /**
     * 새 여행지 추천 설정을 등록한다.
     *
     * 국가와 지역으로 찾은 실제 여행지를 저장해 이후 국가·지역명이 바뀌어도 추천 대상의
     * 참조 무결성을 유지한다.
     *
     * @param request 추천 대상 및 노출 조건
     * @return 생성된 추천 설정
     */
    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminRecommendationResponse.Item create(AdminRecommendationRequest request) {
        var destination = getDestination(request.countryId(), request.regionId());
        validatePeriod(request);
        var recommendation = recommendationRepository.save(Objects.requireNonNull(DestinationRecommendation.builder()
                .destination(destination)
                .imageUrl(normalizeImageUrl(request.imageUrl()))
                .displayStartDt(request.displayStartDt())
                .displayEndDt(request.displayEndDt())
                .sortOrder(normalizeSortOrder(request.sortOrder()))
                .useYn(normalizeUseYn(request.useYn()))
                .build()));
        return AdminRecommendationResponse.Item.from(recommendation);
    }

    /**
     * 기존 추천 설정을 수정한다.
     *
     * 요청의 국가·지역이 변경되면 추천 대상 여행지도 다시 결정한다. 존재하지 않는 추천 ID나
     * 대상 여행지는 각각 404 응답으로 변환한다.
     *
     * @param recommendId 수정할 추천 설정 식별자
     * @param request 변경할 추천 대상 및 노출 조건
     * @return 수정된 추천 설정
     */
    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminRecommendationResponse.Item update(Long recommendId, AdminRecommendationRequest request) {
        var recommendation = recommendationRepository.findWithDestinationByRecommendId(recommendId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 여행지를 찾을 수 없습니다."));
        var destination = getDestination(request.countryId(), request.regionId());
        validatePeriod(request);
        recommendation.update(destination, normalizeImageUrl(request.imageUrl()), request.displayStartDt(), request.displayEndDt(), normalizeSortOrder(request.sortOrder()), normalizeUseYn(request.useYn()));
        return AdminRecommendationResponse.Item.from(recommendation);
    }

    /**
     * 국가와 지역에 등록된 여행지 중 좋아요 수가 가장 높은 여행지를 선택한다.
     *
     * @throws ResponseStatusException 선택한 소속에 여행지가 없을 때
     */
    private com.trip.routemate.destination.domain.Destination getDestination(Long countryId, Long regionId) {
        return destinationRepository
                .findTopByCountryCountryIdAndRegionRegionIdOrderByLikeCountDesc(countryId, regionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "선택한 국가·지역에 등록된 여행지가 없습니다."
                ));
    }

    /** 노출 시작 시각보다 종료 시각이 늦은지 검증한다. */
    private void validatePeriod(AdminRecommendationRequest request) {
        if (!request.displayStartDt().isBefore(request.displayEndDt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "노출 종료일은 시작일보다 늦어야 합니다.");
        }
    }

    /** 정렬 순서가 없거나 1보다 작으면 첫 번째 순서로 보정한다. */
    private int normalizeSortOrder(Integer sortOrder) {
        return sortOrder == null || sortOrder < 1 ? 1 : sortOrder;
    }

    /** 명시적으로 {@code N}인 값만 미사용 상태로 보정한다. */
    private String normalizeUseYn(String useYn) {
        return "N".equalsIgnoreCase(useYn) ? "N" : "Y";
    }

    /** 공백 이미지는 미지정 값으로 바꿔 여행지 기본 이미지를 사용할 수 있게 한다. */
    private String normalizeImageUrl(String imageUrl) {
        return imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim();
    }
}
