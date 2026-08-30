package com.trip.routemate.lotto.dto;

import com.trip.routemate.lotto.client.LottoHistoryClient;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 최신 또는 특정 회차 주변의 로또 당첨번호 목록을 제공하는 응답이다.
 *
 * @param latestDrawNumber 현재 확인된 최신 회차 번호
 * @param draws 조회 기준 주변의 당첨 회차 목록
 */
public record LottoDrawHistoryResponse(int latestDrawNumber, List<Draw> draws) {

    /** 한 회차의 당첨번호, 보너스 번호, 등수별 당첨 정보를 화면에 제공한다.
     * @param drawNumber 회차 번호
     * @param numbers 당첨번호 여섯 개
     * @param bonusNumber 보너스 번호
     * @param prizes 1등부터 5등까지의 당첨 정보
     */
    public record Draw(int drawNumber, List<Integer> numbers, int bonusNumber, List<Prize> prizes) {
        /** 외부 API 회차를 번호 오름차순의 공개 응답으로 변환한다. */
        public static Draw from(LottoHistoryClient.LottoDraw draw) {
            return new Draw(
                    draw.drawNumber(),
                    draw.numbers().stream().sorted().toList(),
                    draw.bonusNumber(),
                    draw.prizes().stream().map(Prize::from).toList()
            );
        }
    }

    /** 한 등수의 당첨자 수와 금액을 제공한다.
     * @param rank 당첨 등수
     * @param winnerCount 해당 등수 당첨자 수
     * @param amount 1인당 당첨금
     * @param totalAmount 해당 등수 총 당첨금
     * @param estimatedTaxAmount 1인당 당첨금 기준 예상 세금
     * @param estimatedNetAmount 1인당 당첨금 기준 예상 실수령액
     */
    public record Prize(
            int rank,
            long winnerCount,
            long amount,
            long totalAmount,
            long estimatedTaxAmount,
            long estimatedNetAmount
    ) {
        private static final long TAX_EXEMPT_LIMIT = 2_000_000L;
        private static final long HIGH_RATE_THRESHOLD = 300_000_000L;
        private static final long STANDARD_TAX_RATE_PERCENT = 22L;
        private static final long HIGH_TAX_RATE_PERCENT = 33L;

        /** 외부 API의 등수별 당첨 정보를 공개 응답으로 변환한다. */
        public static Prize from(LottoHistoryClient.LottoDraw.Prize prize) {
            TaxResult taxResult = calculateTax(prize.amount());
            return new Prize(
                    prize.rank(),
                    prize.winnerCount(),
                    prize.amount(),
                    prize.totalAmount(),
                    taxResult.estimatedTaxAmount(),
                    taxResult.estimatedNetAmount()
            );
        }

        /** 1인당 당첨금의 예상 세금과 실수령액을 계산한다. */
        private static TaxResult calculateTax(long prizeAmount) {
            if (prizeAmount <= TAX_EXEMPT_LIMIT) {
                return new TaxResult(0L, prizeAmount);
            }

            long estimatedTaxAmount = prizeAmount <= HIGH_RATE_THRESHOLD
                    ? prizeAmount * STANDARD_TAX_RATE_PERCENT / 100L
                    : HIGH_RATE_THRESHOLD * STANDARD_TAX_RATE_PERCENT / 100L
                            + (prizeAmount - HIGH_RATE_THRESHOLD) * HIGH_TAX_RATE_PERCENT / 100L;
            return new TaxResult(estimatedTaxAmount, prizeAmount - estimatedTaxAmount);
        }

        /** 내부 계산에만 사용하는 예상 세금과 실수령액 쌍이다. */
        private record TaxResult(long estimatedTaxAmount, long estimatedNetAmount) {
        }
    }

    /** 외부 이력 페이지를 최신 회차 순으로 정리해 응답으로 변환한다. */
    public static LottoDrawHistoryResponse from(int latestDrawNumber, List<LottoHistoryClient.LottoDraw> draws) {
        return new LottoDrawHistoryResponse(
                latestDrawNumber,
                draws.stream()
                        .map(Draw::from)
                        .sorted(Comparator.<Draw>comparingInt(draw ->
                                Objects.requireNonNull(draw, "로또 회차 정보가 필요합니다.").drawNumber()
                        ).reversed())
                        .toList()
        );
    }
}
