package com.trip.routemate.plan.controller;

import com.trip.routemate.plan.dto.CreateTravelPlanRequest;
import com.trip.routemate.plan.dto.TravelPlanResponse;
import com.trip.routemate.plan.dto.TravelPlanDetailResponse;
import com.trip.routemate.plan.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-travel-plans")
@Validated
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @GetMapping
    public ResponseEntity<List<TravelPlanResponse>> getMyTravelPlans(Authentication authentication) {
        return ResponseEntity.ok(travelPlanService.getMyTravelPlans(resolveUserEmail(authentication)));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<TravelPlanDetailResponse> getTravelPlan(
            Authentication authentication,
            @PathVariable Long planId
    ) {
        return ResponseEntity.ok(travelPlanService.getTravelPlan(resolveUserEmail(authentication), planId));
    }

    @PostMapping
    public ResponseEntity<TravelPlanResponse> createTravelPlan(
            Authentication authentication,
            @Valid
            @RequestBody CreateTravelPlanRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(travelPlanService.createTravelPlan(resolveUserEmail(authentication), request));
    }

    private String resolveUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
