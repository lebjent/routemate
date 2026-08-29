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

/**
 * 국가, 지역, 여행지 마스터 데이터를 관리하는 관리자 API다.
 *
 * 국가와 지역은 여행지의 소속 정보를 결정한다. 따라서 수정·등록 요청의 실제 소속 관계와
 * 중복 검증은 {@link AdminDestinationService}에서 수행하며, 이 클래스는 요청 검증과
 * HTTP 상태 코드만 담당한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/destinations")
@Tag(name = "Admin Destinations", description = "국가·지역·장소 및 장소 카테고리 관리 API")
public class AdminDestinationController {
    private final AdminDestinationService adminDestinationService;

    /**
     * 국가 목록과 상태별 요약을 조회한다.
     *
     * @param query 국가명 또는 국가 코드 검색어. 빈 값이면 전체를 조회한다.
     * @param status 사용 상태 필터. {@code ALL}이면 상태를 제한하지 않는다.
     * @return 국가 목록과 집계 정보
    */
    @GetMapping("/countries")
    @Operation(summary = "국가 목록 조회")
    public ResponseEntity<AdminDestinationResponse> getCountries(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        return ResponseEntity.ok(adminDestinationService.getCountries(query, status));
    }

    /**
     * 국가를 등록한다.
     *
     * 국가 코드는 시스템에서 고유해야 하며, 위반 시 서비스가 업무 예외로 변환한다.
     *
     * @param request 국가명, 코드, 사용 상태
     * @return 생성된 국가 정보와 HTTP 201
    */
    @PostMapping("/countries")
    @Operation(summary = "국가 등록")
    public ResponseEntity<AdminDestinationResponse.CountryItem> createCountry(
            @Valid @RequestBody AdminCountryRequest request
    ) {
        return ResponseEntity.status(201).body(adminDestinationService.createCountry(request));
    }

    /**
     * 기존 국가의 이름, 코드, 사용 상태를 수정한다.
     *
     * @param countryId 수정할 국가 식별자
     * @param request 변경할 국가 정보
     * @return 수정된 국가 정보
    */
    @PatchMapping("/countries/{countryId}")
    @Operation(summary = "국가 수정")
    public ResponseEntity<AdminDestinationResponse.CountryItem> updateCountry(
            @PathVariable("countryId") Long countryId,
            @Valid @RequestBody AdminCountryRequest request
    ) {
        return ResponseEntity.ok(adminDestinationService.updateCountry(countryId, request));
    }

    /**
     * 지정한 국가에 속한 지역 목록을 조회한다.
     *
     * @param countryId 지역을 조회할 국가 식별자
     * @return 정렬 순서가 반영된 지역 목록
    */
    @GetMapping("/countries/{countryId}/regions")
    @Operation(summary = "국가별 지역 목록 조회")
    public ResponseEntity<java.util.List<AdminDestinationResponse.RegionItem>> getRegions(
            @PathVariable("countryId") Long countryId
    ) {
        return ResponseEntity.ok(adminDestinationService.getRegions(countryId));
    }

    /**
     * 지정 국가에 새 지역을 등록한다.
     *
     * @param countryId 상위 국가 식별자
     * @param request 지역명, 코드, 정렬 순서, 사용 상태
     * @return 생성된 지역 정보와 HTTP 201
    */
    @PostMapping("/countries/{countryId}/regions")
    @Operation(summary = "지역 등록")
    public ResponseEntity<AdminDestinationResponse.RegionItem> createRegion(
            @PathVariable("countryId") Long countryId,
            @Valid @RequestBody AdminRegionRequest request
    ) {
        return ResponseEntity.status(201).body(adminDestinationService.createRegion(countryId, request));
    }

    /**
     * 지정 국가에 실제로 속한 지역만 수정한다.
     *
     * @param countryId 상위 국가 식별자
     * @param regionId 수정할 지역 식별자
     * @param request 변경할 지역 정보
     * @return 수정된 지역 정보
    */
    @PatchMapping("/countries/{countryId}/regions/{regionId}")
    @Operation(summary = "지역 수정")
    public ResponseEntity<AdminDestinationResponse.RegionItem> updateRegion(
            @PathVariable("countryId") Long countryId,
            @PathVariable("regionId") Long regionId,
            @Valid @RequestBody AdminRegionRequest request
    ) {
        return ResponseEntity.ok(adminDestinationService.updateRegion(countryId, regionId, request));
    }

    /**
     * 국가 또는 지역 조건에 맞는 여행지 목록을 조회한다.
     *
     * @param countryId 선택 조건인 국가 식별자
     * @param regionId 선택 조건인 지역 식별자
     * @return 조건에 맞는 여행지 목록
    */
    @GetMapping("/places")
    @Operation(summary = "장소 목록 조회")
    public ResponseEntity<AdminDestinationPlaceResponse> getPlaces(
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) Long regionId
    ) {
        return ResponseEntity.ok(adminDestinationService.getPlaces(countryId, regionId));
    }

    /**
     * 여행지 등록 폼에서 사용할 고정 카테고리 목록을 조회한다.
     *
     * @return 카테고리 코드와 표시명 목록
    */
    @GetMapping("/place-categories")
    @Operation(summary = "장소 카테고리 목록 조회")
    public ResponseEntity<AdminPlaceCategoryResponse> getPlaceCategories() {
        return ResponseEntity.ok(adminDestinationService.getPlaceCategories());
    }

    /**
     * 국가·지역 소속 관계가 유효한 여행지를 등록한다.
     *
     * @param request 여행지 기본 정보, 위치, 카테고리
     * @return 생성된 여행지 정보와 HTTP 201
    */
    @PostMapping("/places")
    @Operation(summary = "장소 등록")
    public ResponseEntity<AdminDestinationPlaceResponse.PlaceItem> createPlace(
            @Valid @RequestBody AdminDestinationPlaceRequest request
    ) {
        return ResponseEntity.status(201).body(adminDestinationService.createPlace(request));
    }

    /**
     * 여행지 정보를 수정한다.
     *
     * @param destinationId 수정할 여행지 식별자
     * @param request 변경할 여행지 정보
     * @return 수정된 여행지 정보
    */
    @PatchMapping("/places/{destinationId}")
    @Operation(summary = "장소 수정")
    public ResponseEntity<AdminDestinationPlaceResponse.PlaceItem> updatePlace(
            @PathVariable("destinationId") Long destinationId,
            @Valid @RequestBody AdminDestinationPlaceRequest request
    ) {
        return ResponseEntity.ok(adminDestinationService.updatePlace(destinationId, request));
    }
}
