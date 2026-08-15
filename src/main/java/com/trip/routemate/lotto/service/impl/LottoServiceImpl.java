package com.trip.routemate.lotto.service.impl;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;
import com.trip.routemate.lotto.service.LottoHistoryFrequencyService;
import com.trip.routemate.lotto.service.LottoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LottoServiceImpl implements LottoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final LottoHistoryFrequencyService lottoHistoryFrequencyService;

    @Override
    public List<Integer> generateLottoNumbers() {
        return RANDOM.ints(1, 46)
                .distinct()
                .limit(6)
                .sorted()
                .boxed()
                .toList();
    }

    @Override
    public LottoFrequencyResponse generateFrequentLottoNumbers() {
        return lottoHistoryFrequencyService.generateCombination();
    }
}
