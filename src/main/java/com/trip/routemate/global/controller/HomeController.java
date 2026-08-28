package com.trip.routemate.global.controller;

import com.trip.routemate.global.service.HomeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeQueryService homeQueryService;

    /**
     * 메인 화면 데이터 조회 API
     * GET http://localhost:8090/api/home/data
     */
    @GetMapping("/api/home/data")
    public ResponseEntity<HomeQueryService.HomeDataResponse> getHomeData() {
        return ResponseEntity.ok(homeQueryService.getHomeData());
    }
}
