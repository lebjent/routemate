package com.trip.routemate.common.concurrent;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 공통 제한 병렬 처리의 기본 동시 실행 수를 외부 설정으로 받는다.
 *
 * @param maxThreads 동시에 실행할 최대 작업 수. 외부 API와 데이터베이스의 부담을 고려해 설정한다.
 */
@Validated
@ConfigurationProperties(prefix = "app.parallel")
public record ParallelTaskProperties(@Min(1) int maxThreads) {
}
