package com.trip.routemate.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * 개발 환경에서 업로드 파일을 보관하고 공개하는 로컬 저장소 설정이다.
 *
 * 실제 파일은 도메인별 폴더의 {@code yyyy/MM/dd} 하위 경로에 저장한다.
 * 신규 이미지의 상대 경로는 TB_STORED_IMAGE에 기록하고, URL 접두사는 기존 이미지 조회에 사용한다.
 */
@ConfigurationProperties(prefix = "app.storage")
public record LocalStorageProperties(
        String rootDirectory,
        String optionProductDirectory,
        String travelPlanDirectory,
        String optionProductUrlPrefix,
        String travelPlanUrlPrefix
) {
    /** 옵션상품 대표 이미지가 실제로 저장되는 절대 경로를 반환한다. */
    public Path optionProductDirectoryPath() {
        return Path.of(rootDirectory).toAbsolutePath().normalize().resolve(optionProductDirectory).normalize();
    }

    /** 일정 대표 이미지가 실제로 저장될 절대 경로를 반환한다. */
    public Path travelPlanDirectoryPath() {
        return Path.of(rootDirectory).toAbsolutePath().normalize().resolve(travelPlanDirectory).normalize();
    }

    /** 옵션상품 URL 접두사의 마지막 슬래시를 제거한다. */
    public String normalizedOptionProductUrlPrefix() {
        return normalizeUrlPrefix(optionProductUrlPrefix);
    }

    /** 일정 대표 이미지 URL 접두사의 마지막 슬래시를 제거한다. */
    public String normalizedTravelPlanUrlPrefix() {
        return normalizeUrlPrefix(travelPlanUrlPrefix);
    }

    private String normalizeUrlPrefix(String urlPrefix) {
        return urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
    }
}
