package com.trip.routemate.lotto.controller;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import com.trip.routemate.lotto.service.LottoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
