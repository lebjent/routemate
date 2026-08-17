package com.trip.routemate.admin.controller;

import com.trip.routemate.admin.dto.AdminCountryRequest;
import com.trip.routemate.admin.dto.AdminDestinationResponse;
import com.trip.routemate.admin.dto.AdminRegionRequest;
import com.trip.routemate.admin.dto.AdminDestinationPlaceRequest;
import com.trip.routemate.admin.dto.AdminDestinationPlaceResponse;
import com.trip.routemate.admin.service.AdminDestinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/destinations")
public class AdminDestinationController {
    private final AdminDestinationService adminDestinationService;

    @GetMapping("/countries")
    public ResponseEntity<AdminDestinationResponse> getCountries(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "ALL") String status) { return ResponseEntity.ok(adminDestinationService.getCountries(query, status)); }
    @PostMapping("/countries")
    public ResponseEntity<AdminDestinationResponse.CountryItem> createCountry(@Valid @RequestBody AdminCountryRequest request) { return ResponseEntity.status(201).body(adminDestinationService.createCountry(request)); }
    @PatchMapping("/countries/{countryId}")
    public ResponseEntity<AdminDestinationResponse.CountryItem> updateCountry(@PathVariable Long countryId, @Valid @RequestBody AdminCountryRequest request) { return ResponseEntity.ok(adminDestinationService.updateCountry(countryId, request)); }
    @GetMapping("/countries/{countryId}/regions")
    public ResponseEntity<java.util.List<AdminDestinationResponse.RegionItem>> getRegions(@PathVariable Long countryId) { return ResponseEntity.ok(adminDestinationService.getRegions(countryId)); }
    @PostMapping("/countries/{countryId}/regions")
    public ResponseEntity<AdminDestinationResponse.RegionItem> createRegion(@PathVariable Long countryId, @Valid @RequestBody AdminRegionRequest request) { return ResponseEntity.status(201).body(adminDestinationService.createRegion(countryId, request)); }
    @PatchMapping("/countries/{countryId}/regions/{regionId}")
    public ResponseEntity<AdminDestinationResponse.RegionItem> updateRegion(@PathVariable Long countryId, @PathVariable Long regionId, @Valid @RequestBody AdminRegionRequest request) { return ResponseEntity.ok(adminDestinationService.updateRegion(countryId, regionId, request)); }

    @GetMapping("/places")
    public ResponseEntity<AdminDestinationPlaceResponse> getPlaces(@RequestParam(required = false) Long countryId, @RequestParam(required = false) Long regionId) { return ResponseEntity.ok(adminDestinationService.getPlaces(countryId, regionId)); }

    @PostMapping("/places")
    public ResponseEntity<AdminDestinationPlaceResponse.PlaceItem> createPlace(@Valid @RequestBody AdminDestinationPlaceRequest request) { return ResponseEntity.status(201).body(adminDestinationService.createPlace(request)); }

    @PatchMapping("/places/{destinationId}")
    public ResponseEntity<AdminDestinationPlaceResponse.PlaceItem> updatePlace(@PathVariable Long destinationId, @Valid @RequestBody AdminDestinationPlaceRequest request) { return ResponseEntity.ok(adminDestinationService.updatePlace(destinationId, request)); }
}
