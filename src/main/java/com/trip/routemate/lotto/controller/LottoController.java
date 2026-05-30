package com.trip.routemate.lotto.controller;

import com.trip.routemate.lotto.service.LottoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor // LottoService 자동 주입
public class LottoController {
    private final LottoService lottoService;

    @GetMapping("/lotto")
    public String home(Model model) {
        // 로또 번호 생성 후 Model에 추가
        List<Integer> lottoNumbers = lottoService.generateLottoNumbers();
        model.addAttribute("lottoNumbers", lottoNumbers);

        return "lotto/luckyLotto";
    }
}
