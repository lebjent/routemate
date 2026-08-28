package com.trip.routemate.destination.controller;

import com.trip.routemate.destination.dto.CountryResponse;
import com.trip.routemate.destination.dto.RegionResponse;
import com.trip.routemate.destination.service.DestinationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/destinations")
@Tag(name = "Destinations", description = "공개 국가·지역 조회 API")
public class DestinationController {

    private final DestinationQueryService destinationQueryService;

    @GetMapping("/countries")
    @Operation(summary = "국가 목록 조회", description = "여행 상품과 여행 계획 작성에 사용할 활성 국가 목록을 조회합니다.")
    public ResponseEntity<List<CountryResponse>> getCountries() {
        return ResponseEntity.ok(destinationQueryService.getCountries());
    }

    @GetMapping("/countries/{countryCode}/regions")
    @Operation(summary = "국가별 지역 조회", description = "국가 코드에 해당하는 지역을 정렬 순서대로 조회합니다.")
    public ResponseEntity<List<RegionResponse>> getRegionsByCountry(@PathVariable("countryCode") String countryCode) {
        return ResponseEntity.ok(destinationQueryService.getRegions(countryCode));
    }
}
