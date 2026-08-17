package com.trip.routemate.plan.controller;

import com.trip.routemate.plan.dto.TravelPlanDetailResponse;
import com.trip.routemate.plan.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/travel-plans")
public class PublicTravelPlanController {

    private final TravelPlanService travelPlanService;

    @GetMapping("/{planId}")
    public ResponseEntity<TravelPlanDetailResponse> getPublicTravelPlan(@PathVariable("planId") Long planId) {
        return ResponseEntity.ok(travelPlanService.getPublicTravelPlan(planId));
    }
}
