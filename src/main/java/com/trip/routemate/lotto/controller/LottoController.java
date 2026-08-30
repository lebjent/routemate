package com.trip.routemate.lotto.controller;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import com.trip.routemate.lotto.dto.LottoDrawHistoryResponse;
import com.trip.routemate.lotto.dto.LottoRandomSimulationResponse;
import com.trip.routemate.lotto.service.LottoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 무작위 로또 번호와 역대 빈도 기반 추천 번호를 제공하는 공개 API다.
 *
 * 빈도 기반 추천은 외부 이력 데이터의 조회 상태에 따라 일시적으로 사용할 수 없을 수 있다.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Lotto", description = "로또 번호 생성 및 과거 당첨 번호 빈도 API")
public class LottoController {
    private final LottoService lottoService;

    /**
     * 중복 없는 무작위 로또 번호 여섯 개를 오름차순으로 반환한다.
     *
     * @return 1부터 45 사이의 정수 여섯 개
     */
    @GetMapping("/api/lotto/numbers")
    @Operation(summary = "로또 번호 생성")
    public ResponseEntity<List<Integer>> getLottoNumbers() {
        return ResponseEntity.ok(lottoService.generateLottoNumbers());
    }

    /** 일반 랜덤 추첨을 10,000회 반복한 상위 출현 번호를 반환한다. */
    @GetMapping("/api/lotto/random-statistics")
    @Operation(summary = "일반 랜덤 시뮬레이션 통계 조회")
    public ResponseEntity<LottoRandomSimulationResponse> getRandomStatistics() {
        return ResponseEntity.ok(lottoService.simulateRandomDraws());
    }

    /**
     * 역대 당첨번호 빈도가 높은 후보에서 추천 번호를 생성한다.
     *
     * @return 추천 번호, 번호별 빈도, 분석 회차 등 통계 정보
     */
    @GetMapping("/api/lotto/frequent-numbers")
    @Operation(summary = "로또 번호 빈도 조회")
    public ResponseEntity<LottoFrequencyResponse> getFrequentLottoNumbers() {
        return ResponseEntity.ok(lottoService.generateFrequentLottoNumbers());
    }

    /** 최신 회차와 최근 당첨번호 목록을 조회한다. */
    @GetMapping("/api/lotto/draws/latest")
    @Operation(summary = "최신 로또 회차 조회")
    public ResponseEntity<LottoDrawHistoryResponse> getLatestDraws() {
        return ResponseEntity.ok(lottoService.getLatestDraws());
    }

    /** 지정 회차를 기준으로 외부 이력 API가 제공하는 주변 회차를 조회한다. */
    @GetMapping("/api/lotto/draws")
    @Operation(summary = "역대 로또 회차 조회")
    public ResponseEntity<LottoDrawHistoryResponse> getDrawsAround(@RequestParam int drawNumber) {
        return ResponseEntity.ok(lottoService.getDrawsAround(drawNumber));
    }
}
