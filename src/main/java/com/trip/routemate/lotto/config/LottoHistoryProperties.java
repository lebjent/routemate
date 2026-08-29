package com.trip.routemate.lotto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 로또 이력 API 주소와 통계 갱신 정책을 외부 설정으로 받는다.
 *
 * @param sourceUrl 공식 이력 조회 API 주소
 * @param refreshInterval 메모리 통계 캐시의 유효 기간
 * @param topNumberCount 추천 후보로 유지할 상위 빈도 번호 수
 */
@ConfigurationProperties(prefix = "app.lotto.history")
public record LottoHistoryProperties(
        String sourceUrl,
        Duration refreshInterval,
        int topNumberCount
) {
}
