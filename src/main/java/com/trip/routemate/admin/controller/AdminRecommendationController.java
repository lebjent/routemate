package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminRecommendationRequest;
import com.trip.routemate.admin.dto.AdminRecommendationResponse;
import com.trip.routemate.admin.service.AdminRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/recommendations")
public class AdminRecommendationController {
    private final AdminRecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<AdminRecommendationResponse> getRecommendations() { return ResponseEntity.ok(recommendationService.getRecommendations()); }

    @PostMapping
    public ResponseEntity<AdminRecommendationResponse.Item> create(@Valid @RequestBody AdminRecommendationRequest request) { return ResponseEntity.status(201).body(recommendationService.create(request)); }

    @PatchMapping("/{recommendId}")
    public ResponseEntity<AdminRecommendationResponse.Item> update(@PathVariable("recommendId") Long recommendId, @Valid @RequestBody AdminRecommendationRequest request) { return ResponseEntity.ok(recommendationService.update(recommendId, request)); }
}
