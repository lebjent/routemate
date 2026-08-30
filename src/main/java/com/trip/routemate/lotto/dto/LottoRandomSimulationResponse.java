package com.trip.routemate.lotto.dto;

import java.time.Instant;
import java.util.List;

/**
 * 무작위 로또 추첨을 여러 번 반복한 번호 출현 통계 응답이다.
 *
 * 실제 당첨 이력이 아닌 난수 시뮬레이션 결과이므로, 미래 당첨 확률을 예측하거나 높이지 않는다.
 *
 * @param simulatedDrawCount 시뮬레이션한 추첨 횟수
 * @param topNumbers 출현 횟수 내림차순의 상위 번호 목록
 * @param generatedAt 시뮬레이션을 생성한 시각
 */
public record LottoRandomSimulationResponse(
        int simulatedDrawCount,
        List<NumberFrequency> topNumbers,
        Instant generatedAt
) {

    /** 시뮬레이션에서 한 번호가 등장한 횟수다.
     * @param number 로또 번호
     * @param count 시뮬레이션에서의 출현 횟수
     */
    public record NumberFrequency(int number, int count) {
    }
}
