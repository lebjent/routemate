package com.trip.routemate.destination.repository;

import com.trip.routemate.destination.domain.Country;
import com.trip.routemate.destination.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/** 지역 마스터 조회와 저장을 담당하는 리포지터리다. */
public interface RegionRepository extends JpaRepository<Region, Long> {
    @EntityGraph(attributePaths = "country")
    List<Region> findByCountryOrderBySortOrderAscRegionNameAsc(Country country);

    Optional<Region> findByCountryAndRegionCode(Country country, String regionCode);

    Optional<Region> findByRegionIdAndCountry(Long regionId, Country country);

    long countByCountry(Country country);
}
