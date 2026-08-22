package com.trip.routemate.lotto.service;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LottoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final LottoHistoryFrequencyService lottoHistoryFrequencyService;

    public List<Integer> generateLottoNumbers() {
        return RANDOM.ints(1, 46)
                .distinct()
                .limit(6)
                .sorted()
                .boxed()
                .toList();
    }

    public LottoFrequencyResponse generateFrequentLottoNumbers() {
        return lottoHistoryFrequencyService.generateCombination();
    }
}
