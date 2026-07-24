package com.trip.routemate.lotto.controller;

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
        // 로또 번호 생성 후 리스트 형태로 반환
        return ResponseEntity.ok(lottoService.generateLottoNumbers());
    }
}
