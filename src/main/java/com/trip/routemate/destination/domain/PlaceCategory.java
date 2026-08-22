package com.trip.routemate.destination.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {
    FOOD("맛집"),
    SIGHTSEEING("관광지"),
    SHOPPING("쇼핑"),
    ACCOMMODATION("숙박"),
    CAFE("카페"),
    NATURE("자연·공원"),
    CULTURE("문화·전시"),
    ACTIVITY("체험·액티비티");

    private final String label;
}
