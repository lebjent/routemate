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

/**
 * 공개 설정된 여행 계획을 누구나 조회할 수 있도록 제공하는 API다.
 *
 * 비공개 일정은 이 경로에서 조회할 수 없으며, 소유자는 내 일정 API를 사용해야 한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/travel-plans")
@Tag(name = "Public Travel Plans", description = "공개된 여행 계획 상세 조회 API")
public class PublicTravelPlanController {

    private final TravelPlanService travelPlanService;

    /**
     * 공개 여행 계획의 일차, 방문 지역, 일정, 준비물을 상세 조회한다.
     *
     * @param planId 조회할 공개 일정 식별자
     * @return 공개 일정 상세 정보
     */
    @GetMapping("/{planId}")
    @Operation(summary = "공개 여행 계획 상세 조회")
    public ResponseEntity<TravelPlanDetailResponse> getPublicTravelPlan(@PathVariable("planId") Long planId) {
        return ResponseEntity.ok(travelPlanService.getPublicTravelPlan(planId));
    }
}
