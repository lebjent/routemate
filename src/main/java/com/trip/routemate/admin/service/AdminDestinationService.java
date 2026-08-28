package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminCountryRequest;
import com.trip.routemate.admin.dto.AdminDestinationResponse;
import com.trip.routemate.admin.dto.AdminRegionRequest;
import com.trip.routemate.admin.dto.AdminDestinationPlaceRequest;
import com.trip.routemate.admin.dto.AdminDestinationPlaceResponse;
import com.trip.routemate.admin.dto.AdminPlaceCategoryResponse;
import com.trip.routemate.destination.domain.Country;
import com.trip.routemate.destination.domain.Region;
import com.trip.routemate.destination.repository.CountryRepository;
import com.trip.routemate.destination.repository.RegionRepository;
import com.trip.routemate.destination.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDestinationService {
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final DestinationRepository destinationRepository;

    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationResponse getCountries(String query, String status) {
        var normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        var normalizedStatus = normalizeStatus(status);
        var countries = countryRepository.findAllByOrderByCountryNameAsc().stream()
                .filter(country -> ("ALL".equals(normalizedStatus) || normalizedStatus.equals(country.getCountryStatCd())))
                .filter(country -> normalizedQuery.isBlank()
                        || country.getCountryName().toLowerCase().contains(normalizedQuery)
                        || country.getCountryCode().toLowerCase().contains(normalizedQuery))
                .toList();
        var allCountries = countryRepository.findAll();
        var active = allCountries.stream().filter(country -> "ACTIVE".equals(country.getCountryStatCd())).count();
        var totalRegions = allCountries.stream().mapToLong(regionRepository::countByCountry).sum();
        var items = countries.stream().map(country -> AdminDestinationResponse.CountryItem.from(country, regionRepository.countByCountry(country))).toList();
        return new AdminDestinationResponse(new AdminDestinationResponse.Summary(allCountries.size(), active, allCountries.size() - active, totalRegions), items);
    }

    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public java.util.List<AdminDestinationResponse.RegionItem> getRegions(Long countryId) {
        var country = getCountry(countryId);
        return regionRepository.findByCountryOrderBySortOrderAscRegionNameAsc(country).stream().map(AdminDestinationResponse.RegionItem::from).toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationResponse.CountryItem createCountry(AdminCountryRequest request) {
        var name = request.countryName().trim();
        var code = request.countryCode().trim().toUpperCase();
        if (countryRepository.findByCountryName(name).isPresent() || countryRepository.findByCountryCode(code).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 국가명 또는 국가 코드입니다.");
        var country = countryRepository.save(Objects.requireNonNull(
                Country.builder().countryName(name).countryCode(code)
                        .countryStatCd(normalizeStatusForSave(request.countryStatCd())).build()
        ));
        return AdminDestinationResponse.CountryItem.from(country, 0);
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationResponse.CountryItem updateCountry(Long countryId, AdminCountryRequest request) {
        var country = getCountry(countryId);
        var name = request.countryName().trim();
        var code = request.countryCode().trim().toUpperCase();
        countryRepository.findByCountryName(name).filter(found -> !found.getCountryId().equals(countryId)).ifPresent(found -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 국가명입니다."); });
        countryRepository.findByCountryCode(code).filter(found -> !found.getCountryId().equals(countryId)).ifPresent(found -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 국가 코드입니다."); });
        country.update(name, code, normalizeStatusForSave(request.countryStatCd()));
        return AdminDestinationResponse.CountryItem.from(country, regionRepository.countByCountry(country));
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationResponse.RegionItem createRegion(Long countryId, AdminRegionRequest request) {
        var country = getCountry(countryId);
        var code = request.regionCode().trim().toUpperCase();
        if (regionRepository.findByCountryAndRegionCode(country, code).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 지역 코드입니다.");
        var region = regionRepository.save(Objects.requireNonNull(
                Region.builder().country(country).regionName(request.regionName().trim()).regionCode(code)
                        .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder())
                        .regionStatCd(normalizeStatusForSave(request.regionStatCd())).build()
        ));
        return AdminDestinationResponse.RegionItem.from(region);
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationResponse.RegionItem updateRegion(Long countryId, Long regionId, AdminRegionRequest request) {
        var country = getCountry(countryId);
        var region = regionRepository.findByRegionIdAndCountry(regionId, country).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지역을 찾을 수 없습니다."));
        var code = request.regionCode().trim().toUpperCase();
        regionRepository.findByCountryAndRegionCode(country, code).filter(found -> !found.getRegionId().equals(regionId)).ifPresent(found -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 지역 코드입니다."); });
        region.update(request.regionName().trim(), code, request.sortOrder() == null ? 0 : request.sortOrder(), normalizeStatusForSave(request.regionStatCd()));
        return AdminDestinationResponse.RegionItem.from(region);
    }

    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationPlaceResponse getPlaces(Long countryId, Long regionId) {
        var places = destinationRepository.findAllByOrderByDestNameAsc().stream()
                .filter(place -> countryId == null || place.getCountry().getCountryId().equals(countryId))
                .filter(place -> regionId == null || place.getRegion().getRegionId().equals(regionId))
                .map(AdminDestinationPlaceResponse.PlaceItem::from)
                .toList();
        return new AdminDestinationPlaceResponse(places);
    }

    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminPlaceCategoryResponse getPlaceCategories() {
        return AdminPlaceCategoryResponse.fromCategories();
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationPlaceResponse.PlaceItem createPlace(AdminDestinationPlaceRequest request) {
        var country = getCountry(request.countryId());
        var region = regionRepository.findByRegionIdAndCountry(request.regionId(), country)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 국가에 속한 지역이 아닙니다."));
        var place = destinationRepository.save(Objects.requireNonNull(com.trip.routemate.destination.domain.Destination.builder()
                .destName(request.destName().trim()).destDesc(request.destDesc()).country(country).region(region)
                .category(request.category()).imageUrl(normalizeImageUrl(request.imageUrl()))
                .mapLat(request.mapLat()).mapLng(request.mapLng()).likeCount(0).build()));
        return AdminDestinationPlaceResponse.PlaceItem.from(place);
    }

    @Transactional
    @PreAuthorize("hasAuthority('DESTINATION_MANAGE')")
    public AdminDestinationPlaceResponse.PlaceItem updatePlace(Long destinationId, AdminDestinationPlaceRequest request) {
        var place = destinationRepository.findWithCountryAndRegionByDestId(destinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다."));
        var country = getCountry(request.countryId());
        var region = regionRepository.findByRegionIdAndCountry(request.regionId(), country)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 국가에 속한 지역이 아닙니다."));
        place.update(request.destName().trim(), request.destDesc(), country, region, request.category(), normalizeImageUrl(request.imageUrl()), request.mapLat(), request.mapLng());
        return AdminDestinationPlaceResponse.PlaceItem.from(place);
    }

    private Country getCountry(Long countryId) { return countryRepository.findById(Objects.requireNonNull(countryId, "국가 ID가 필요합니다.")).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다.")); }
    private String normalizeStatus(String status) { var value = status == null ? "ALL" : status.trim().toUpperCase(); if ("ALL".equals(value) || "ACTIVE".equals(value) || "INACTIVE".equals(value)) return value; throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 상태입니다."); }
    private String normalizeStatusForSave(String status) { var value = status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase(); if ("ACTIVE".equals(value) || "INACTIVE".equals(value)) return value; throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 상태입니다."); }
    private String normalizeImageUrl(String imageUrl) { return imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim(); }
}
