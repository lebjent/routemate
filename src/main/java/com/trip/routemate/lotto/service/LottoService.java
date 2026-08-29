package com.trip.routemate.lotto.service;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

/** 로또 번호 생성 방식별 요청을 처리하는 진입 서비스다. */
@Service
@RequiredArgsConstructor
public class LottoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final LottoHistoryFrequencyService lottoHistoryFrequencyService;

    /** 1부터 45까지의 번호 중 중복 없는 여섯 개를 무작위로 생성한다. */
    public List<Integer> generateLottoNumbers() {
        return RANDOM.ints(1, 46)
                .distinct()
                .limit(6)
                .sorted()
                .boxed()
                .toList();
    }

    /** 역대 당첨번호 빈도를 바탕으로 추천 조합을 생성한다. */
    public LottoFrequencyResponse generateFrequentLottoNumbers() {
        return lottoHistoryFrequencyService.generateCombination();
    }
}
