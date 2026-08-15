package com.trip.routemate.lotto.service;

import com.trip.routemate.lotto.dto.LottoFrequencyResponse;

import java.util.List;

public interface LottoService {
    List<Integer> generateLottoNumbers();

    LottoFrequencyResponse generateFrequentLottoNumbers();
}
