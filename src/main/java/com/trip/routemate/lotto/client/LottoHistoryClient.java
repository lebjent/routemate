package com.trip.routemate.lotto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trip.routemate.lotto.config.LottoHistoryProperties;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

/** 동행복권 이력 API 호출과 재시도 정책을 전담합니다. */
@Component
@NullMarked
public class LottoHistoryClient {

    private final RestClient restClient;
    private final LottoHistoryProperties properties;

    public LottoHistoryClient(RestClient.Builder restClientBuilder, LottoHistoryProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    @Retry(name = "lotto-history")
    public List<LottoDraw> requestDrawPage(int drawNumber) {
        var response = restClient.get()
                .uri(requestUrl(drawNumber))
                .retrieve()
                .body(new ParameterizedTypeReference<OfficialLottoHistoryResponse>() {
                });
        if (response == null || response.data() == null || response.data().draws() == null) {
            return List.of();
        }
        return response.data().draws();
    }

    @NonNull
    @SuppressWarnings("null") // JDT가 JDK String.formatted()의 non-null 반환 계약을 알지 못하는 오탐입니다.
    private String requestUrl(int drawNumber) {
        return "%s?srchDir=center&srchLtEpsd=%d".formatted(
                Objects.requireNonNull(properties.sourceUrl(), "로또 이력 API 주소가 필요합니다."),
                drawNumber
        );
    }

    private record OfficialLottoHistoryResponse(OfficialLottoHistoryData data) {
    }

    private record OfficialLottoHistoryData(@JsonProperty("list") List<LottoDraw> draws) {
    }

    public record LottoDraw(
            @JsonProperty("ltEpsd") int drawNumber,
            @JsonProperty("tm1WnNo") int firstNumber,
            @JsonProperty("tm2WnNo") int secondNumber,
            @JsonProperty("tm3WnNo") int thirdNumber,
            @JsonProperty("tm4WnNo") int fourthNumber,
            @JsonProperty("tm5WnNo") int fifthNumber,
            @JsonProperty("tm6WnNo") int sixthNumber
    ) {
        public List<Integer> numbers() {
            return List.of(firstNumber, secondNumber, thirdNumber, fourthNumber, fifthNumber, sixthNumber);
        }
    }
}
