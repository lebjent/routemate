package com.trip.routemate.lotto.dto;

import com.trip.routemate.lotto.client.LottoHistoryClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 로또 당첨금 원천징수 예상 계산의 경계 금액을 검증한다. */
class LottoDrawHistoryResponseTest {

    @Test
    void twoMillionWonOrLessIsTaxExempt() {
        var prize = toResponsePrize(2_000_000L);

        assertThat(prize.estimatedTaxAmount()).isZero();
        assertThat(prize.estimatedNetAmount()).isEqualTo(2_000_000L);
    }

    @Test
    void amountOverTwoMillionWonUsesTwentyTwoPercentOnTheWholePrize() {
        var prize = toResponsePrize(2_000_001L);

        assertThat(prize.estimatedTaxAmount()).isEqualTo(440_000L);
        assertThat(prize.estimatedNetAmount()).isEqualTo(1_560_001L);
    }

    @Test
    void amountOverThreeHundredMillionWonUsesTheHigherRateOnlyForTheExcess() {
        var prize = toResponsePrize(1_000_000_000L);

        assertThat(prize.estimatedTaxAmount()).isEqualTo(297_000_000L);
        assertThat(prize.estimatedNetAmount()).isEqualTo(703_000_000L);
    }

    private LottoDrawHistoryResponse.Prize toResponsePrize(long amount) {
        return LottoDrawHistoryResponse.Prize.from(new LottoHistoryClient.LottoDraw.Prize(1, 1L, amount, amount));
    }
}
