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

/**
 * 로그인 사용자의 여행 계획을 생성, 조회, 수정하는 API다.
 *
 * 사용자 식별자는 인증 정보에서만 가져오며 URL이나 본문으로 받지 않는다. 서비스 계층은
 * 모든 상세 조회와 수정에서 일정 소유자를 확인해 다른 사용자의 일정 접근을 차단한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-travel-plans")
@Validated
@Tag(name = "Travel Plans", description = "로그인 사용자의 여행 일정 생성·조회·수정 API")
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    /**
     * 현재 사용자가 만든 여행 계획 목록을 조회한다.
     *
     * @param authentication 현재 로그인 사용자 인증 정보
     * @return 최신 여행 계획 목록
     */
    @GetMapping
    @Operation(summary = "내 여행 계획 목록 조회")
    public ResponseEntity<List<TravelPlanResponse>> getMyTravelPlans(Authentication authentication) {
        return ResponseEntity.ok(travelPlanService.getMyTravelPlans(resolveUserEmail(authentication)));
    }

    /**
     * 현재 사용자가 소유한 여행 계획의 상세 정보를 조회한다.
     *
     * @param authentication 현재 로그인 사용자 인증 정보
     * @param planId 조회할 여행 계획 식별자
     * @return 일차, 지역, 일정, 교통편, 준비물을 포함한 상세 정보
     */
    @GetMapping("/{planId}")
    @Operation(summary = "내 여행 계획 상세 조회")
    public ResponseEntity<TravelPlanDetailResponse> getTravelPlan(
            Authentication authentication,
            @PathVariable("planId") Long planId
    ) {
        return ResponseEntity.ok(travelPlanService.getTravelPlan(resolveUserEmail(authentication), planId));
    }

    /**
     * 일차별 일정과 준비물을 포함한 새 여행 계획을 생성한다.
     *
     * @param authentication 현재 로그인 사용자 인증 정보
     * @param request 여행 기간, 공개 여부, 일차별 세부 일정
     * @return 생성된 여행 계획 요약과 HTTP 201
     */
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

    /**
     * 현재 사용자가 소유한 여행 계획 전체를 요청 값으로 교체한다.
     *
     * @param authentication 현재 로그인 사용자 인증 정보
     * @param planId 수정할 여행 계획 식별자
     * @param request 교체할 여행 계획 전체 정보
     * @return 수정된 여행 계획 요약
     */
    @PutMapping("/{planId}")
    @Operation(summary = "여행 계획 수정")
    public ResponseEntity<TravelPlanResponse> updateTravelPlan(
            Authentication authentication,
            @PathVariable("planId") Long planId,
            @Valid @RequestBody CreateTravelPlanRequest request
    ) {
        return ResponseEntity.ok(travelPlanService.updateTravelPlan(resolveUserEmail(authentication), planId, request));
    }


    /** 인증 객체에서 로그인 이메일을 안전하게 추출한다.
     *
     * @throws ResponseStatusException 인증되지 않은 요청일 때
     */
    private String resolveUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return authentication.getName();
    }
}
