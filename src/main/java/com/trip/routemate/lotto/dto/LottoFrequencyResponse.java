package com.trip.routemate.lotto.dto;

import java.time.Instant;
import java.util.List;

public record LottoFrequencyResponse(
        List<Integer> numbers,
        List<NumberFrequency> topNumbers,
        int analyzedDrawCount,
        int latestDrawNumber,
        Instant refreshedAt
) {
    public record NumberFrequency(
            int number,
            int count
    ) {
    }
}
