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

/**
 * 동행복권 이력 API와의 HTTP 통신을 전담한다.
 *
 * 회차별 당첨번호 수집이라는 외부 연동 책임만 가진다. 응답 데이터의 통계 계산이나
 * 저장 여부는 호출 서비스가 결정한다. 네트워크 일시 오류는 {@code lotto-history} 재시도 정책에
 * 따라 처리하며, 응답 본문이 비어 있거나 형식이 달라진 경우에는 빈 목록을 반환한다.
 */
@Component
@NullMarked
public class LottoHistoryClient {

    private final RestClient restClient;
    private final LottoHistoryProperties properties;

    /**
     * 외부 API 호출에 사용할 클라이언트와 주소 설정을 주입한다.
     *
     * @param restClientBuilder Spring이 관리하는 HTTP 클라이언트 빌더
     * @param properties 동행복권 이력 API 주소와 수집 설정
     */
    public LottoHistoryClient(RestClient.Builder restClientBuilder, LottoHistoryProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    /**
     * 지정한 회차를 기준으로 동행복권 이력 API의 한 페이지를 조회한다.
     *
     * 통신 실패 시 Resilience4j의 {@code lotto-history} 재시도 정책이 적용된다. HTTP 응답은
     * 성공했지만 데이터가 없거나 예상 JSON 구조와 다르면 수집 작업을 안전하게 계속할 수 있도록
     * 빈 목록을 반환한다.
     *
     * @param drawNumber 조회 기준 회차
     * @return API가 반환한 당첨 회차 목록. 데이터가 없으면 빈 목록
     */
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

    /**
     * 설정된 API 주소와 조회 회차로 요청 URL을 만든다.
     *
     * @param drawNumber 조회 기준 회차
     * @return null이 아닌 동행복권 이력 API 요청 URL
     * @throws NullPointerException API 주소 설정이 누락된 경우
     */
    @NonNull
    @SuppressWarnings("null") // JDT가 JDK String.formatted()의 non-null 반환 계약을 알지 못하는 오탐입니다.
    private String requestUrl(int drawNumber) {
        return "%s?srchDir=center&srchLtEpsd=%d".formatted(
                Objects.requireNonNull(properties.sourceUrl(), "로또 이력 API 주소가 필요합니다."),
                drawNumber
        );
    }

    /**
     * 동행복권 API 최상위 JSON 응답을 역직렬화하는 내부 DTO다.
     *
     * @param data 회차 목록을 포함하는 응답 데이터. 외부 API가 비정상 응답을 주면 null일 수 있다.
     */
    private record OfficialLottoHistoryResponse(OfficialLottoHistoryData data) {
    }

    /**
     * 최상위 응답의 회차 목록 영역을 역직렬화하는 내부 DTO다.
     *
     * @param draws {@code list} JSON 속성의 당첨 회차 목록. 응답에 없으면 null일 수 있다.
     */
    private record OfficialLottoHistoryData(@JsonProperty("list") List<LottoDraw> draws) {
    }

    /**
     * 동행복권 API가 제공하는 한 회차의 당첨번호와 등수별 당첨 정보다.
     *
     * @param drawNumber 회차 번호
     * @param firstNumber 첫 번째 당첨번호
     * @param secondNumber 두 번째 당첨번호
     * @param thirdNumber 세 번째 당첨번호
     * @param fourthNumber 네 번째 당첨번호
     * @param fifthNumber 다섯 번째 당첨번호
     * @param sixthNumber 여섯 번째 당첨번호
     * @param bonusNumber 보너스 번호
     * @param firstPrizeWinnerCount 1등 당첨자 수
     * @param firstPrizeAmount 1등 1인당 당첨금
     * @param firstPrizeTotalAmount 1등 총 당첨금
     * @param secondPrizeWinnerCount 2등 당첨자 수
     * @param secondPrizeAmount 2등 1인당 당첨금
     * @param secondPrizeTotalAmount 2등 총 당첨금
     * @param thirdPrizeWinnerCount 3등 당첨자 수
     * @param thirdPrizeAmount 3등 1인당 당첨금
     * @param thirdPrizeTotalAmount 3등 총 당첨금
     * @param fourthPrizeWinnerCount 4등 당첨자 수
     * @param fourthPrizeAmount 4등 1인당 당첨금
     * @param fourthPrizeTotalAmount 4등 총 당첨금
     * @param fifthPrizeWinnerCount 5등 당첨자 수
     * @param fifthPrizeAmount 5등 1인당 당첨금
     * @param fifthPrizeTotalAmount 5등 총 당첨금
     */
    public record LottoDraw(
            @JsonProperty("ltEpsd") int drawNumber,
            @JsonProperty("tm1WnNo") int firstNumber,
            @JsonProperty("tm2WnNo") int secondNumber,
            @JsonProperty("tm3WnNo") int thirdNumber,
            @JsonProperty("tm4WnNo") int fourthNumber,
            @JsonProperty("tm5WnNo") int fifthNumber,
            @JsonProperty("tm6WnNo") int sixthNumber,
            @JsonProperty("bnsWnNo") int bonusNumber,
            @JsonProperty("rnk1WnNope") long firstPrizeWinnerCount,
            @JsonProperty("rnk1WnAmt") long firstPrizeAmount,
            @JsonProperty("rnk1SumWnAmt") long firstPrizeTotalAmount,
            @JsonProperty("rnk2WnNope") long secondPrizeWinnerCount,
            @JsonProperty("rnk2WnAmt") long secondPrizeAmount,
            @JsonProperty("rnk2SumWnAmt") long secondPrizeTotalAmount,
            @JsonProperty("rnk3WnNope") long thirdPrizeWinnerCount,
            @JsonProperty("rnk3WnAmt") long thirdPrizeAmount,
            @JsonProperty("rnk3SumWnAmt") long thirdPrizeTotalAmount,
            @JsonProperty("rnk4WnNope") long fourthPrizeWinnerCount,
            @JsonProperty("rnk4WnAmt") long fourthPrizeAmount,
            @JsonProperty("rnk4SumWnAmt") long fourthPrizeTotalAmount,
            @JsonProperty("rnk5WnNope") long fifthPrizeWinnerCount,
            @JsonProperty("rnk5WnAmt") long fifthPrizeAmount,
            @JsonProperty("rnk5SumWnAmt") long fifthPrizeTotalAmount
    ) {
        /**
         * 번호 순서를 보존한 불변 목록으로 변환한다.
         *
         * @return 첫 번째 번호부터 여섯 번째 번호까지의 목록
         */
        public List<Integer> numbers() {
            return List.of(firstNumber, secondNumber, thirdNumber, fourthNumber, fifthNumber, sixthNumber);
        }

        /** 등수별 당첨자 수, 1인당 당첨금, 총 당첨금을 순서대로 반환한다. */
        public List<Prize> prizes() {
            return List.of(
                    new Prize(1, firstPrizeWinnerCount, firstPrizeAmount, firstPrizeTotalAmount),
                    new Prize(2, secondPrizeWinnerCount, secondPrizeAmount, secondPrizeTotalAmount),
                    new Prize(3, thirdPrizeWinnerCount, thirdPrizeAmount, thirdPrizeTotalAmount),
                    new Prize(4, fourthPrizeWinnerCount, fourthPrizeAmount, fourthPrizeTotalAmount),
                    new Prize(5, fifthPrizeWinnerCount, fifthPrizeAmount, fifthPrizeTotalAmount)
            );
        }

        /** 외부 API의 한 등수 당첨 정보를 표현한다. */
        public record Prize(int rank, long winnerCount, long amount, long totalAmount) {
        }
    }
}
