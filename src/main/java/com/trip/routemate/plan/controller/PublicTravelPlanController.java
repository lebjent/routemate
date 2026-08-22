package com.trip.routemate.plan.controller;

import com.trip.routemate.plan.dto.TravelPlanDetailResponse;
import com.trip.routemate.plan.service.TravelPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/travel-plans")
@Tag(name = "Public Travel Plans", description = "공개된 여행 계획 상세 조회 API")
public class PublicTravelPlanController {

    private final TravelPlanService travelPlanService;

    @GetMapping("/{planId}")
    @Operation(summary = "공개 여행 계획 상세 조회")
    public ResponseEntity<TravelPlanDetailResponse> getPublicTravelPlan(@PathVariable("planId") Long planId) {
        return ResponseEntity.ok(travelPlanService.getPublicTravelPlan(planId));
    }
}
