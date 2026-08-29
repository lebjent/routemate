package com.trip.routemate.lotto.dto;

import java.time.Instant;
import java.util.List;

/**
 * 역대 로또 번호 빈도 기반 추천 결과를 제공하는 응답이다.
 *
 * @param numbers 추천 번호 여섯 개
 * @param topNumberFrequencies 상위 빈도 번호와 출현 횟수
 * @param analyzedDrawCount 분석에 사용한 유효 회차 수
 * @param latestDrawNumber 분석한 가장 최신 회차 번호
 * @param refreshedAt 통계를 마지막으로 갱신한 시각
 */
public record LottoFrequencyResponse(
        List<Integer> numbers,
        List<NumberFrequency> topNumbers,
        int analyzedDrawCount,
        int latestDrawNumber,
        Instant refreshedAt
) {
    public record NumberFrequency(
            int number,
            int count
    ) {
    }
}
