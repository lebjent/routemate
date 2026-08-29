package com.trip.routemate.lotto.service;

import com.trip.routemate.lotto.client.LottoHistoryClient;
import com.trip.routemate.lotto.config.LottoHistoryProperties;
import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
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
    private final SecureRandom random = new SecureRandom();
    private final Object cacheLock = new Object();
    private volatile CachedStatistics cachedStatistics;

    public LottoHistoryFrequencyService(LottoHistoryClient lottoHistoryClient, LottoHistoryProperties properties) {
        this.lottoHistoryClient = lottoHistoryClient;
        this.properties = properties;
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

    /** 최신 회차부터 첫 회차까지의 이력을 페이지 단위로 요청한다. */
    private List<LottoHistoryClient.LottoDraw> requestOfficialHistory() {
        var latestPage = findLatestDrawPage();
        var history = new ArrayList<>(latestPage.draws());
        for (var drawNumber = latestPage.latestDrawNumber() - 10; drawNumber > 0; drawNumber -= 10) {
            history.addAll(requestDrawPage(drawNumber));
        }
        // The official history endpoint centers its response around the requested draw.
        // Requesting draw 1 ensures the first draw is included at the lower boundary.
        history.addAll(requestDrawPage(1));
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
