package com.trip.routemate.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션에서 사용하는 Spring Cache 기능을 활성화한다.
 *
 * 캐시 저장소와 만료 정책은 환경별 설정 파일에서 관리한다.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {
}
