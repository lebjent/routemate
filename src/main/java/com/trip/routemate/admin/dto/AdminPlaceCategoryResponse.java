package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.PlaceCategory;

import java.util.Arrays;
import java.util.List;

@io.swagger.v3.oas.annotations.media.Schema(description = "장소 등록 시 선택 가능한 카테고리 목록 응답 DTO")
public record AdminPlaceCategoryResponse(List<CategoryItem> categories) {
    public static AdminPlaceCategoryResponse fromCategories() {
        return new AdminPlaceCategoryResponse(Arrays.stream(PlaceCategory.values())
                .map(category -> new CategoryItem(category.name(), category.getLabel()))
                .toList());
    }

    public record CategoryItem(String code, String label) {
    }
}
