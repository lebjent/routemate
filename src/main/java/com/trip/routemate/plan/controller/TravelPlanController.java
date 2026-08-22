package com.trip.routemate.plan.controller;

import com.trip.routemate.plan.dto.CreateTravelPlanRequest;
import com.trip.routemate.plan.dto.TravelPlanResponse;
import com.trip.routemate.plan.dto.TravelPlanDetailResponse;
import com.trip.routemate.plan.service.TravelPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-travel-plans")
@Validated
@Tag(name = "Travel Plans", description = "로그인 사용자의 여행 일정 생성·조회·수정 API")
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @GetMapping
    @Operation(summary = "내 여행 계획 목록 조회")
    public ResponseEntity<List<TravelPlanResponse>> getMyTravelPlans(Authentication authentication) {
        return ResponseEntity.ok(travelPlanService.getMyTravelPlans(resolveUserEmail(authentication)));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "내 여행 계획 상세 조회")
    public ResponseEntity<TravelPlanDetailResponse> getTravelPlan(
            Authentication authentication,
            @PathVariable("planId") Long planId
    ) {
        return ResponseEntity.ok(travelPlanService.getTravelPlan(resolveUserEmail(authentication), planId));
    }

    @PostMapping
    @Operation(summary = "여행 계획 생성", description = "일정, 방문 지역, 교통편과 준비물을 포함한 여행 계획을 생성합니다.")
    public ResponseEntity<TravelPlanResponse> createTravelPlan(
            Authentication authentication,
            @Valid
            @RequestBody CreateTravelPlanRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(travelPlanService.createTravelPlan(resolveUserEmail(authentication), request));
    }

    @PutMapping("/{planId}")
    @Operation(summary = "여행 계획 수정")
    public ResponseEntity<TravelPlanResponse> updateTravelPlan(
            Authentication authentication,
            @PathVariable("planId") Long planId,
            @Valid @RequestBody CreateTravelPlanRequest request
    ) {
        return ResponseEntity.ok(travelPlanService.updateTravelPlan(resolveUserEmail(authentication), planId, request));
    }


    private String resolveUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
