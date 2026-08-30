package com.trip.routemate.lotto.service;

import com.trip.routemate.common.concurrent.ParallelTaskExecutor;
import com.trip.routemate.lotto.client.LottoHistoryClient;
import com.trip.routemate.lotto.config.LottoHistoryProperties;
import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import com.trip.routemate.lotto.dto.LottoDrawHistoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * 공식 로또 이력에서 번호 빈도를 집계하고 추천 조합을 생성한다.
 *
 * 집계 결과는 설정된 주기 동안 메모리에 보관하며, 외부 조회 실패 시 이전 결과를 사용한다.
 */
@Service
public class LottoHistoryFrequencyService {

    private static final int LOTTO_NUMBER_COUNT = 6;
    private static final int MIN_LOTTO_NUMBER = 1;
    private static final int MAX_LOTTO_NUMBER = 45;
    private static final LocalDate FIRST_DRAW_DATE = LocalDate.of(2002, 12, 7);
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final LottoHistoryClient lottoHistoryClient;
    private final LottoHistoryProperties properties;
    private final ParallelTaskExecutor parallelTaskExecutor;
    private final SecureRandom random = new SecureRandom();
    private final Object cacheLock = new Object();
    private volatile CachedStatistics cachedStatistics;

    public LottoHistoryFrequencyService(
            LottoHistoryClient lottoHistoryClient,
            LottoHistoryProperties properties,
            ParallelTaskExecutor parallelTaskExecutor
    ) {
        this.lottoHistoryClient = lottoHistoryClient;
        this.properties = properties;
        this.parallelTaskExecutor = parallelTaskExecutor;
    }

    /** 외부 이력 데이터의 상위 빈도 번호에서 여섯 개를 선택한다. */
    public LottoFrequencyResponse generateCombination() {
        var statistics = loadStatistics();
        var candidates = new ArrayList<>(statistics.topNumbers());
        if (candidates.size() < LOTTO_NUMBER_COUNT) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "로또 통계 데이터가 충분하지 않습니다.");
        }

        java.util.Collections.shuffle(candidates, random);
        var numbers = candidates.stream()
                .limit(LOTTO_NUMBER_COUNT)
                .sorted()
                .toList();
        return new LottoFrequencyResponse(
                numbers,
                statistics.topNumberFrequencies(),
                statistics.analyzedDrawCount(),
                statistics.latestDrawNumber(),
                statistics.refreshedAt()
        );
    }

    /** 최신 회차와 외부 API가 함께 반환한 최근 회차 목록을 조회한다. */
    public LottoDrawHistoryResponse getLatestDraws() {
        var latestPage = findLatestDrawPage();
        return LottoDrawHistoryResponse.from(latestPage.latestDrawNumber(), latestPage.draws());
    }

    /**
     * 지정 회차 주변의 당첨번호를 조회한다.
     *
     * 동행복권 이력 API는 요청 회차를 중심으로 여러 회차를 반환한다.
     */
    public LottoDrawHistoryResponse getDrawsAround(int drawNumber) {
        if (drawNumber < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 회차는 1 이상이어야 합니다.");
        }
        var draws = requestDrawPage(drawNumber);
        var latestDrawNumber = draws.stream()
                .mapToInt(draw -> Objects.requireNonNull(draw, "로또 회차 정보가 필요합니다.").drawNumber())
                .max()
                .orElse(drawNumber);
        return LottoDrawHistoryResponse.from(latestDrawNumber, draws);
    }

    /** 만료되지 않은 통계를 사용하거나 외부 이력으로 새 통계를 만든다. */
    private CachedStatistics loadStatistics() {
        var current = cachedStatistics;
        if (current != null && current.isFresh()) {
            return current;
        }

        synchronized (cacheLock) {
            current = cachedStatistics;
            if (current != null && current.isFresh()) {
                return current;
            }
            try {
                var refreshed = summarize(requestOfficialHistory());
                cachedStatistics = refreshed;
                return refreshed;
            } catch (RestClientException exception) {
                if (current != null) {
                    return current;
                }
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "역대 로또 통계 데이터를 불러오지 못했습니다.", exception);
            }
        }
    }

    /**
     * 최신 회차를 확인한 뒤, 나머지 이력 페이지는 제한된 가상 스레드로 병렬 요청한다.
     *
     * 공통 실행기의 최대 동시 실행 수는 {@code app.parallel.max-threads}로 조절한다. 결과는
     * 요청 회차 순서대로 합쳐지고, 이후 집계 단계에서 중복 회차를 제거한다.
     */
    private List<LottoHistoryClient.LottoDraw> requestOfficialHistory() {
        var latestPage = findLatestDrawPage();
        var history = new ArrayList<>(latestPage.draws());
        var pageAnchors = new ArrayList<Integer>();
        for (var drawNumber = latestPage.latestDrawNumber() - 10; drawNumber > 0; drawNumber -= 10) {
            pageAnchors.add(drawNumber);
        }
        // 공식 이력 API는 요청 회차 주변의 데이터를 반환하므로, 첫 회차를 별도로 보장한다.
        pageAnchors.add(1);
        parallelTaskExecutor.map(
                        pageAnchors,
                        pageAnchor -> requestDrawPage(Objects.requireNonNull(pageAnchor, "로또 회차가 필요합니다.").intValue())
                )
                .forEach(history::addAll);
        return history;
    }

    /** 현재 날짜를 기준으로 추정한 최근 회차 근처에서 실제 최신 회차를 찾는다. */
    private LatestDrawPage findLatestDrawPage() {
        var today = LocalDate.now(KOREA_ZONE_ID);
        var estimatedDrawNumber = (int) ChronoUnit.WEEKS.between(FIRST_DRAW_DATE, today) + 1;
        for (var drawNumber = estimatedDrawNumber; drawNumber >= estimatedDrawNumber - 2; drawNumber--) {
            var draws = requestDrawPage(drawNumber);
            if (!draws.isEmpty()) {
                var latestDrawNumber = draws.stream()
                        .mapToInt(draw -> draw.drawNumber())
                        .max()
                        .orElseThrow();
                return new LatestDrawPage(latestDrawNumber, draws);
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "최신 로또 회차를 확인하지 못했습니다.");
    }

    /** 지정 회차를 기준으로 공식 이력 페이지를 요청한다. */
    private List<LottoHistoryClient.LottoDraw> requestDrawPage(int drawNumber) {
        return lottoHistoryClient.requestDrawPage(drawNumber);
    }

    /** 중복·비정상 회차를 제외하고 번호별 출현 횟수를 집계한다. */
    private CachedStatistics summarize(List<LottoHistoryClient.LottoDraw> history) {
        if (history == null || history.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "역대 로또 통계 데이터가 비어 있습니다.");
        }

        var counts = new int[MAX_LOTTO_NUMBER + 1];
        var validDrawCount = 0;
        var latestDrawNumber = 0;
        var processedDrawNumbers = new HashSet<Integer>();
        for (var draw : history) {
            if (!isValidDraw(draw) || !processedDrawNumbers.add(draw.drawNumber())) {
                continue;
            }
            draw.numbers().forEach(number -> counts[number]++);
            validDrawCount++;
            latestDrawNumber = Math.max(latestDrawNumber, draw.drawNumber());
        }
        if (validDrawCount == 0) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "유효한 역대 로또 당첨번호가 없습니다.");
        }

        var topNumberCount = Math.clamp(properties.topNumberCount(), LOTTO_NUMBER_COUNT, MAX_LOTTO_NUMBER);
        var topNumbers = java.util.stream.IntStream.rangeClosed(MIN_LOTTO_NUMBER, MAX_LOTTO_NUMBER)
                .mapToObj(number -> new LottoFrequencyResponse.NumberFrequency(number, counts[number]))
                .sorted(Comparator.comparingInt((LottoFrequencyResponse.NumberFrequency frequency) -> frequency.count())
                        .reversed()
                        .thenComparingInt(frequency -> frequency.number()))
                .limit(topNumberCount)
                .toList();
        var refreshedAt = Instant.now();
        return new CachedStatistics(topNumbers, validDrawCount, latestDrawNumber, refreshedAt, refreshedAt.plus(properties.refreshInterval()));
    }

    /** 한 회차가 로또 번호 여섯 개와 범위를 모두 충족하는지 검증한다. */
    private boolean isValidDraw(LottoHistoryClient.LottoDraw draw) {
        return draw != null
                && draw.drawNumber() > 0
                && draw.numbers().stream().allMatch(number -> number >= MIN_LOTTO_NUMBER && number <= MAX_LOTTO_NUMBER)
                && draw.numbers().stream().distinct().count() == LOTTO_NUMBER_COUNT;
    }

    /** 최신 회차를 판별할 때 사용하는 이력 페이지다. */
    private record LatestDrawPage(
            int latestDrawNumber,
            List<LottoHistoryClient.LottoDraw> draws
    ) {
    }

    /** 추천 생성에 재사용하는 빈도 집계 캐시다. */
    private record CachedStatistics(
            List<LottoFrequencyResponse.NumberFrequency> topNumberFrequencies,
            int analyzedDrawCount,
            int latestDrawNumber,
            Instant refreshedAt,
            Instant expiresAt
    ) {
        /** 현재 시각 기준으로 캐시가 만료되지 않았는지 확인한다. */
        private boolean isFresh() {
            return Instant.now().isBefore(expiresAt);
        }

        /** 출현 빈도가 높은 번호만 추출한다. */
        private List<Integer> topNumbers() {
            return topNumberFrequencies.stream()
                    .map(frequency -> frequency.number())
                    .toList();
        }
    }
}
