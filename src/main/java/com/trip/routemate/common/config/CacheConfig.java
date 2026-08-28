package com.trip.routemate.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    /**
     * 공개 조회 데이터만 짧게 보관합니다.
     * 관리 화면 수정 사항이 오래 남지 않도록 TTL을 1분으로 제한합니다.
     */
    @Bean
    CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager("homeData", "destinationCountries", "destinationRegions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(1)));
        return cacheManager;
    }
}
