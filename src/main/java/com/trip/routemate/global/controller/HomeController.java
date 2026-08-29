package com.trip.routemate.global.controller;

import com.trip.routemate.global.service.HomeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/** 서비스 홈 화면에 필요한 추천 콘텐츠를 제공하는 공개 API다. */
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeQueryService homeQueryService;

    /**
     * 추천 여행지와 인기 공개 일정을 한 번에 조회한다.
     *
     * 홈 데이터는 짧은 기간 캐시되어 반복 조회 시 데이터베이스 부하를 줄인다.
     *
     * @return 홈 화면 구성 데이터
     */
    @GetMapping("/api/home/data")
    public ResponseEntity<HomeQueryService.HomeDataResponse> getHomeData() {
        return ResponseEntity.ok(homeQueryService.getHomeData());
    }
}
