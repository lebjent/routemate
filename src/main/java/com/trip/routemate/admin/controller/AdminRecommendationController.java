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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/recommendations")
@Tag(name = "Admin Recommendations", description = "홈 화면 여행지 추천 콘텐츠 관리 API")
public class AdminRecommendationController {
    private final AdminRecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "추천 목록 조회")
    public ResponseEntity<AdminRecommendationResponse> getRecommendations() { return ResponseEntity.ok(recommendationService.getRecommendations()); }

    @PostMapping
    @Operation(summary = "추천 콘텐츠 등록")
    public ResponseEntity<AdminRecommendationResponse.Item> create(@Valid @RequestBody AdminRecommendationRequest request) { return ResponseEntity.status(201).body(recommendationService.create(request)); }

    @PatchMapping("/{recommendId}")
    @Operation(summary = "추천 콘텐츠 수정")
    public ResponseEntity<AdminRecommendationResponse.Item> update(@PathVariable("recommendId") Long recommendId, @Valid @RequestBody AdminRecommendationRequest request) { return ResponseEntity.ok(recommendationService.update(recommendId, request)); }
}
