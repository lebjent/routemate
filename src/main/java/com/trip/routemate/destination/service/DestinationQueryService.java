package com.trip.routemate.destination.service;

import com.trip.routemate.destination.dto.CountryResponse;
import com.trip.routemate.destination.dto.RegionResponse;
import com.trip.routemate.destination.repository.CountryRepository;
import com.trip.routemate.destination.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 여행 계획과 상품 등록에서 사용하는 국가·지역 기준 데이터를 조회한다.
 *
 * 공개 화면과 입력 화면이 같은 기준 데이터를 사용하도록 조회와 응답 변환을 한곳에 둔다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationQueryService {

    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;

    /** 국가 목록을 화면 표시용 응답으로 변환하고 캐시한다. */
    @Cacheable("destinationCountries")
    public List<CountryResponse> getCountries() {
        return countryRepository.findAll().stream()
                .map(CountryResponse::from)
                .toList();
    }

    /** 국가 코드에 속한 지역을 정렬 순서로 조회하고 국가별로 캐시한다. */
    @Cacheable(cacheNames = "destinationRegions", key = "#countryCode")
    public List<RegionResponse> getRegions(String countryCode) {
        var country = countryRepository.findByCountryCode(countryCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다."));
        return regionRepository.findByCountryOrderBySortOrderAscRegionNameAsc(country).stream()
                .map(region -> RegionResponse.from(region, country.getCountryId(), country.getCountryCode()))
                .toList();
    }
}
