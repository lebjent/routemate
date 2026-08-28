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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationQueryService {

    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;

    @Cacheable("destinationCountries")
    public List<CountryResponse> getCountries() {
        return countryRepository.findAll().stream()
                .map(CountryResponse::from)
                .toList();
    }

    @Cacheable(cacheNames = "destinationRegions", key = "#countryCode")
    public List<RegionResponse> getRegions(String countryCode) {
        var country = countryRepository.findByCountryCode(countryCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다."));
        return regionRepository.findByCountryOrderBySortOrderAscRegionNameAsc(country).stream()
                .map(region -> RegionResponse.from(region, country.getCountryId(), country.getCountryCode()))
                .toList();
    }
}
