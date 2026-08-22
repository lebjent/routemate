package com.trip.routemate.admin.dto;

import com.trip.routemate.destination.domain.PlaceCategory;

import java.util.Arrays;
import java.util.List;

public record AdminPlaceCategoryResponse(List<CategoryItem> categories) {
    public static AdminPlaceCategoryResponse fromCategories() {
        return new AdminPlaceCategoryResponse(Arrays.stream(PlaceCategory.values())
                .map(category -> new CategoryItem(category.name(), category.getLabel()))
                .toList());
    }

    public record CategoryItem(String code, String label) {
    }
}
