package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminRecommendationRequest;
import com.trip.routemate.admin.dto.AdminRecommendationResponse;
import com.trip.routemate.admin.service.AdminRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 홈 화면에 노출되는 여행지 추천 콘텐츠를 관리하는 관리자 API다.
 *
 * 추천 설정은 여행지, 노출 기간, 정렬 순서, 사용 상태로 구성된다. 실제 노출 여부는
 * 홈 조회 시점에 사용 상태와 기간을 함께 검증해 결정한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/recommendations")
@Tag(name = "Admin Recommendations", description = "홈 화면 여행지 추천 콘텐츠 관리 API")
public class AdminRecommendationController {
    private final AdminRecommendationService recommendationService;

    /**
     * 등록된 추천 설정과 추천 대상으로 선택 가능한 여행지를 함께 조회한다.
     *
     * @return 추천 설정 목록과 여행지 선택 목록
    */
    @GetMapping
    @Operation(summary = "추천 목록 조회")
    public ResponseEntity<AdminRecommendationResponse> getRecommendations() {
        return ResponseEntity.ok(recommendationService.getRecommendations());
    }

    /**
     * 홈 화면에 노출할 여행지 추천 설정을 등록한다.
     *
     * @param request 대상 여행지, 노출 기간, 정렬 순서, 사용 상태
     * @return 생성된 추천 설정과 HTTP 201
    */
    @PostMapping
    @Operation(summary = "추천 콘텐츠 등록")
    public ResponseEntity<AdminRecommendationResponse.Item> create(
            @Valid @RequestBody AdminRecommendationRequest request
    ) {
        return ResponseEntity.status(201).body(recommendationService.create(request));
    }

    /**
     * 기존 추천 콘텐츠의 여행지, 노출 기간, 정렬 순서, 상태를 수정한다.
     *
     * @param recommendId 수정할 추천 설정 식별자
     * @param request 변경할 추천 설정 정보
     * @return 수정된 추천 설정
    */
    @PatchMapping("/{recommendId}")
    @Operation(summary = "추천 콘텐츠 수정")
    public ResponseEntity<AdminRecommendationResponse.Item> update(
            @PathVariable("recommendId") Long recommendId,
            @Valid @RequestBody AdminRecommendationRequest request
    ) {
        return ResponseEntity.ok(recommendationService.update(recommendId, request));
    }
}
