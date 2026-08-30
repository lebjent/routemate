package com.trip.routemate.lotto.service;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import com.trip.routemate.lotto.dto.LottoDrawHistoryResponse;
import com.trip.routemate.lotto.dto.LottoRandomSimulationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/** 로또 번호 생성 방식별 요청을 처리하는 진입 서비스다. */
@Service
@RequiredArgsConstructor
public class LottoService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RANDOM_SIMULATION_DRAW_COUNT = 10_000;
    private static final int RANDOM_SIMULATION_TOP_NUMBER_COUNT = 10;

    private final LottoHistoryFrequencyService lottoHistoryFrequencyService;

    /** 1부터 45까지의 번호 중 중복 없는 여섯 개를 무작위로 생성한다. */
    public List<Integer> generateLottoNumbers() {
        return RANDOM.ints(1, 46)
                .distinct()
                .limit(6)
                .sorted()
                .boxed()
                .toList();
    }

    /**
     * 무작위 추첨을 10,000회 반복해 가장 자주 등장한 번호를 집계한다.
     *
     * 각 회차는 실제 번호 생성과 동일하게 1부터 45 사이의 중복 없는 여섯 개로 구성한다.
     */
    public LottoRandomSimulationResponse simulateRandomDraws() {
        var counts = new int[46];
        for (var drawIndex = 0; drawIndex < RANDOM_SIMULATION_DRAW_COUNT; drawIndex++) {
            generateLottoNumbers().forEach(number -> counts[number]++);
        }

        var topNumbers = IntStream.rangeClosed(1, 45)
                .mapToObj(number -> new LottoRandomSimulationResponse.NumberFrequency(number, counts[number]))
                .sorted(Comparator.<LottoRandomSimulationResponse.NumberFrequency>comparingInt(frequency ->
                                Objects.requireNonNull(frequency, "로또 시뮬레이션 번호 정보가 필요합니다.").count()
                        )
                        .reversed()
                        .thenComparingInt(frequency ->
                                Objects.requireNonNull(frequency, "로또 시뮬레이션 번호 정보가 필요합니다.").number()
                        ))
                .limit(RANDOM_SIMULATION_TOP_NUMBER_COUNT)
                .toList();
        return new LottoRandomSimulationResponse(RANDOM_SIMULATION_DRAW_COUNT, topNumbers, Instant.now());
    }

    /** 역대 당첨번호 빈도를 바탕으로 추천 조합을 생성한다. */
    public LottoFrequencyResponse generateFrequentLottoNumbers() {
        return lottoHistoryFrequencyService.generateCombination();
    }

    /** 최신 회차와 최근 당첨번호 목록을 조회한다. */
    public LottoDrawHistoryResponse getLatestDraws() {
        return lottoHistoryFrequencyService.getLatestDraws();
    }

    /** 지정 회차 주변의 당첨번호 목록을 조회한다. */
    public LottoDrawHistoryResponse getDrawsAround(int drawNumber) {
        return lottoHistoryFrequencyService.getDrawsAround(drawNumber);
    }
}
