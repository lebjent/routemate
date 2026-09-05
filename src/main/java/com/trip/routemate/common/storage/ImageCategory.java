package com.trip.routemate.common.storage;

/** 이미지 용도별 NAS 하위 폴더. 클라이언트가 임의 경로를 지정할 수 없다. */
public enum ImageCategory {
    PRODUCT("optionProduct"), PRODUCT_DETAIL("optionProductDetail"),
    DESTINATION("destination"), RECOMMENDATION("recommendation"), TRAVEL_PLAN("travelPlan");

    private final String directory;
    ImageCategory(String directory) { this.directory = directory; }
    public String directory() { return directory; }
}
