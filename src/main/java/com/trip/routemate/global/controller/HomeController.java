package com.trip.routemate.global.controller;

import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.plan.dto.TravelPlanResponse;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final DestinationRepository destinationRepository;
    private final TravelPlanRepository travelPlanRepository;

    /**
     * 메인 화면 데이터 조회 API
     * GET http://localhost:8090/api/home/data
     */
    @GetMapping("/api/home/data")
    public ResponseEntity<Map<String, Object>> getHomeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("destinations", destinationRepository.findTop3ByOrderByLikeCountDesc());
        data.put("plans", travelPlanRepository.findTop5ByIsPublicOrderByViewCountDescPlanIdDesc("Y")
                .stream()
                .map(TravelPlanResponse::from)
                .toList());
        return ResponseEntity.ok(data);
    }

}

