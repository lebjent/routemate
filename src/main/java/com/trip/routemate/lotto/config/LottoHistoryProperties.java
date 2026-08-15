package com.trip.routemate.lotto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.lotto.history")
public record LottoHistoryProperties(
        String sourceUrl,
        Duration refreshInterval,
        int topNumberCount
) {
}
