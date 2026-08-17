package com.trip.routemate.lotto.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trip.routemate.lotto.config.LottoHistoryProperties;
import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
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

@Service
public class LottoHistoryFrequencyService {

    private static final int LOTTO_NUMBER_COUNT = 6;
    private static final int MIN_LOTTO_NUMBER = 1;
    private static final int MAX_LOTTO_NUMBER = 45;
    private static final LocalDate FIRST_DRAW_DATE = LocalDate.of(2002, 12, 7);
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final RestClient restClient;
    private final LottoHistoryProperties properties;
    private final SecureRandom random = new SecureRandom();
    private final Object cacheLock = new Object();
    private volatile CachedStatistics cachedStatistics;

    public LottoHistoryFrequencyService(RestClient.Builder restClientBuilder, LottoHistoryProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

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

    private List<OfficialLottoDraw> requestOfficialHistory() {
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

    private List<OfficialLottoDraw> requestDrawPage(int drawNumber) {
        var requestUrl = "%s?srchDir=center&srchLtEpsd=%d".formatted(properties.sourceUrl(), drawNumber);
        var response = restClient.get()
                .uri(requestUrl)
                .retrieve()
                .body(new ParameterizedTypeReference<OfficialLottoHistoryResponse>() {
                });
        if (response == null || response.data() == null || response.data().draws() == null) {
            return List.of();
        }
        return response.data().draws();
    }

    private CachedStatistics summarize(List<OfficialLottoDraw> history) {
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
                .sorted(Comparator.comparingInt(LottoFrequencyResponse.NumberFrequency::count)
                        .reversed()
                        .thenComparingInt(LottoFrequencyResponse.NumberFrequency::number))
                .limit(topNumberCount)
                .toList();
        var refreshedAt = Instant.now();
        return new CachedStatistics(topNumbers, validDrawCount, latestDrawNumber, refreshedAt, refreshedAt.plus(properties.refreshInterval()));
    }

    private boolean isValidDraw(OfficialLottoDraw draw) {
        return draw != null
                && draw.drawNumber() > 0
                && draw.numbers().stream().allMatch(number -> number >= MIN_LOTTO_NUMBER && number <= MAX_LOTTO_NUMBER)
                && draw.numbers().stream().distinct().count() == LOTTO_NUMBER_COUNT;
    }

    private record OfficialLottoHistoryResponse(
            OfficialLottoHistoryData data
    ) {
    }

    private record OfficialLottoHistoryData(
            @JsonProperty("list") List<OfficialLottoDraw> draws
    ) {
    }

    private record OfficialLottoDraw(
            @JsonProperty("ltEpsd") int drawNumber,
            @JsonProperty("tm1WnNo") int firstNumber,
            @JsonProperty("tm2WnNo") int secondNumber,
            @JsonProperty("tm3WnNo") int thirdNumber,
            @JsonProperty("tm4WnNo") int fourthNumber,
            @JsonProperty("tm5WnNo") int fifthNumber,
            @JsonProperty("tm6WnNo") int sixthNumber
    ) {
        private List<Integer> numbers() {
            return List.of(firstNumber, secondNumber, thirdNumber, fourthNumber, fifthNumber, sixthNumber);
        }
    }

    private record LatestDrawPage(
            int latestDrawNumber,
            List<OfficialLottoDraw> draws
    ) {
    }

    private record CachedStatistics(
            List<LottoFrequencyResponse.NumberFrequency> topNumberFrequencies,
            int analyzedDrawCount,
            int latestDrawNumber,
            Instant refreshedAt,
            Instant expiresAt
    ) {
        private boolean isFresh() {
            return Instant.now().isBefore(expiresAt);
        }

        private List<Integer> topNumbers() {
            return topNumberFrequencies.stream()
                    .map(LottoFrequencyResponse.NumberFrequency::number)
                    .toList();
        }
    }
}
