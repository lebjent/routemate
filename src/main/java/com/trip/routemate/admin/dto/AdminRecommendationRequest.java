package com.trip.routemate.admin.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 홈 화면 여행지 추천의 노출 조건을 변경하는 요청이다.
 *
 * 현재 추천 모델은 국가와 지역으로 대상 여행지를 찾는다. 동일한 국가·지역에 여러 여행지가
 * 있으면 서비스가 좋아요 수가 가장 높은 여행지를 추천 대상으로 선택한다.
 *
 * @param countryId 추천 대상 여행지가 속한 국가 식별자
 * @param regionId 추천 대상 여행지가 속한 지역 식별자
 * @param imageUrl 홈 화면에서 사용할 대표 이미지 주소. 비어 있으면 여행지 기본 이미지를 사용한다.
 * @param displayStartDt 추천 노출을 시작할 시각
 * @param displayEndDt 추천 노출을 종료할 시각. 시작 시각보다 늦어야 한다.
 * @param sortOrder 같은 기간의 추천 사이에서 적용할 표시 순서. 없거나 1 미만이면 1을 사용한다.
 * @param useYn 추천 사용 여부. {@code N}만 미사용으로 처리하고 나머지는 사용으로 처리한다.
 */
public record AdminRecommendationRequest(
        @NotNull Long countryId,
        @NotNull Long regionId,
        String imageUrl,
        @NotNull LocalDateTime displayStartDt,
        @NotNull LocalDateTime displayEndDt,
        Integer sortOrder,
        String useYn
) {
}
