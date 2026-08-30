package com.trip.routemate.common.config;

import com.trip.routemate.common.storage.LocalStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 개발용 로컬 업로드 폴더를 이미지 URL로 제공한다.
 *
 * 예를 들어 {@code /uploads/optionProduct/a.png} 요청은 옵션상품 폴더의 파일을,
 * {@code /uploads/travelPlan/a.png} 요청은 일정 폴더의 파일을 반환한다.
 */
@Configuration
@RequiredArgsConstructor
public class LocalStorageWebConfig implements WebMvcConfigurer {
    private final LocalStorageProperties storageProperties;

    /** 로컬 상품 이미지 폴더와 공개 URL 경로를 연결한다. */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler(storageProperties.normalizedOptionProductUrlPrefix() + "/**")
                .addResourceLocations(storageProperties.optionProductDirectoryPath().toUri().toString())
                .setCachePeriod(3600);
        registry.addResourceHandler(storageProperties.normalizedTravelPlanUrlPrefix() + "/**")
                .addResourceLocations(storageProperties.travelPlanDirectoryPath().toUri().toString())
                .setCachePeriod(3600);
    }
}
