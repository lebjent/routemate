package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminCountryRequest;
import com.trip.routemate.admin.dto.AdminDestinationResponse;
import com.trip.routemate.admin.dto.AdminRegionRequest;
import com.trip.routemate.admin.dto.AdminDestinationPlaceRequest;
import com.trip.routemate.admin.dto.AdminDestinationPlaceResponse;
import com.trip.routemate.admin.dto.AdminPlaceCategoryResponse;
import com.trip.routemate.admin.service.AdminDestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/destinations")
@Tag(name = "Admin Destinations", description = "국가·지역·장소 및 장소 카테고리 관리 API")
public class AdminDestinationController {
    private final AdminDestinationService adminDestinationService;

    @GetMapping("/countries")
    @Operation(summary = "국가 목록 조회")
    public ResponseEntity<AdminDestinationResponse> getCountries(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "ALL") String status) { return ResponseEntity.ok(adminDestinationService.getCountries(query, status)); }
    @PostMapping("/countries")
    @Operation(summary = "국가 등록")
    public ResponseEntity<AdminDestinationResponse.CountryItem> createCountry(@Valid @RequestBody AdminCountryRequest request) { return ResponseEntity.status(201).body(adminDestinationService.createCountry(request)); }
    @PatchMapping("/countries/{countryId}")
    @Operation(summary = "국가 수정")
    public ResponseEntity<AdminDestinationResponse.CountryItem> updateCountry(@PathVariable("countryId") Long countryId, @Valid @RequestBody AdminCountryRequest request) { return ResponseEntity.ok(adminDestinationService.updateCountry(countryId, request)); }
    @GetMapping("/countries/{countryId}/regions")
    @Operation(summary = "국가별 지역 목록 조회")
    public ResponseEntity<java.util.List<AdminDestinationResponse.RegionItem>> getRegions(@PathVariable("countryId") Long countryId) { return ResponseEntity.ok(adminDestinationService.getRegions(countryId)); }
    @PostMapping("/countries/{countryId}/regions")
    @Operation(summary = "지역 등록")
    public ResponseEntity<AdminDestinationResponse.RegionItem> createRegion(@PathVariable("countryId") Long countryId, @Valid @RequestBody AdminRegionRequest request) { return ResponseEntity.status(201).body(adminDestinationService.createRegion(countryId, request)); }
    @PatchMapping("/countries/{countryId}/regions/{regionId}")
    @Operation(summary = "지역 수정")
    public ResponseEntity<AdminDestinationResponse.RegionItem> updateRegion(@PathVariable("countryId") Long countryId, @PathVariable("regionId") Long regionId, @Valid @RequestBody AdminRegionRequest request) { return ResponseEntity.ok(adminDestinationService.updateRegion(countryId, regionId, request)); }

    @GetMapping("/places")
    @Operation(summary = "장소 목록 조회")
    public ResponseEntity<AdminDestinationPlaceResponse> getPlaces(@RequestParam(required = false) Long countryId, @RequestParam(required = false) Long regionId) { return ResponseEntity.ok(adminDestinationService.getPlaces(countryId, regionId)); }

    @GetMapping("/place-categories")
    @Operation(summary = "장소 카테고리 목록 조회")
    public ResponseEntity<AdminPlaceCategoryResponse> getPlaceCategories() { return ResponseEntity.ok(adminDestinationService.getPlaceCategories()); }

    @PostMapping("/places")
    @Operation(summary = "장소 등록")
    public ResponseEntity<AdminDestinationPlaceResponse.PlaceItem> createPlace(@Valid @RequestBody AdminDestinationPlaceRequest request) { return ResponseEntity.status(201).body(adminDestinationService.createPlace(request)); }

    @PatchMapping("/places/{destinationId}")
    @Operation(summary = "장소 수정")
    public ResponseEntity<AdminDestinationPlaceResponse.PlaceItem> updatePlace(@PathVariable("destinationId") Long destinationId, @Valid @RequestBody AdminDestinationPlaceRequest request) { return ResponseEntity.ok(adminDestinationService.updatePlace(destinationId, request)); }
}
