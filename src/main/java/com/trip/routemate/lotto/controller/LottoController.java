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

@RestController
@RequiredArgsConstructor // LottoService 자동 주입
@Tag(name = "Lotto", description = "로또 번호 생성 및 과거 당첨 번호 빈도 API")
public class LottoController {
    private final LottoService lottoService;

    @GetMapping("/api/lotto/numbers")
    @Operation(summary = "로또 번호 생성")
    public ResponseEntity<List<Integer>> getLottoNumbers() {
        return ResponseEntity.ok(lottoService.generateLottoNumbers());
    }

    @GetMapping("/api/lotto/frequent-numbers")
    @Operation(summary = "로또 번호 빈도 조회")
    public ResponseEntity<LottoFrequencyResponse> getFrequentLottoNumbers() {
        return ResponseEntity.ok(lottoService.generateFrequentLottoNumbers());
    }
}
