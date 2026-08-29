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

/**
 * 여행 계획과 상품 탐색에서 사용할 국가·지역 데이터를 제공하는 공개 API다.
 *
 * 사용자 입력의 기준 데이터이므로 사용 중인 국가와 지역만 노출한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/destinations")
@Tag(name = "Destinations", description = "공개 국가·지역 조회 API")
public class DestinationController {

    private final DestinationQueryService destinationQueryService;

    /**
     * 활성 상태의 국가 목록을 표시 순서대로 조회한다.
     *
     * @return 국가 코드와 표시명 목록
     */
    @GetMapping("/countries")
    @Operation(summary = "국가 목록 조회", description = "여행 상품과 여행 계획 작성에 사용할 활성 국가 목록을 조회합니다.")
    public ResponseEntity<List<CountryResponse>> getCountries() {
        return ResponseEntity.ok(destinationQueryService.getCountries());
    }

    /**
     * 지정 국가에 속한 활성 지역 목록을 조회한다.
     *
     * @param countryCode 조회할 국가 코드
     * @return 국가에 속한 지역 코드와 표시명 목록
     */
    @GetMapping("/countries/{countryCode}/regions")
    @Operation(summary = "국가별 지역 조회", description = "국가 코드에 해당하는 지역을 정렬 순서대로 조회합니다.")
    public ResponseEntity<List<RegionResponse>> getRegionsByCountry(@PathVariable("countryCode") String countryCode) {
        return ResponseEntity.ok(destinationQueryService.getRegions(countryCode));
    }
}
