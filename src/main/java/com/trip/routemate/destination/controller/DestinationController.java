package com.trip.routemate.destination.controller;

import com.trip.routemate.destination.dto.CountryResponse;
import com.trip.routemate.destination.dto.RegionResponse;
import com.trip.routemate.destination.repository.CountryRepository;
import com.trip.routemate.destination.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/destinations")
public class DestinationController {

    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponse>> getCountries() {
        return ResponseEntity.ok(
                countryRepository.findAll().stream()
                        .map(CountryResponse::from)
                        .toList()
        );
    }

    @GetMapping("/countries/{countryCode}/regions")
    public ResponseEntity<List<RegionResponse>> getRegionsByCountry(@PathVariable("countryCode") String countryCode) {
        var country = countryRepository.findByCountryCode(countryCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다."));

        return ResponseEntity.ok(
                regionRepository.findByCountryOrderBySortOrderAscRegionNameAsc(country).stream()
                        .map(region -> RegionResponse.from(region, country.getCountryId(), country.getCountryCode()))
                        .toList()
        );
    }
}
