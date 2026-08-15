package com.trip.routemate.lotto.controller;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import com.trip.routemate.lotto.service.LottoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor // LottoService 자동 주입
public class LottoController {
    private final LottoService lottoService;

    @GetMapping("/api/lotto/numbers")
    public ResponseEntity<List<Integer>> getLottoNumbers() {
        return ResponseEntity.ok(lottoService.generateLottoNumbers());
    }

    @GetMapping("/api/lotto/frequent-numbers")
    public ResponseEntity<LottoFrequencyResponse> getFrequentLottoNumbers() {
        return ResponseEntity.ok(lottoService.generateFrequentLottoNumbers());
    }
}
