package com.trip.routemate.destination.repository;

import com.trip.routemate.destination.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
/** 국가 마스터 조회와 저장을 담당하는 리포지터리다. */
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCountryCode(String countryCode);

    Optional<Country> findByCountryName(String countryName);

    List<Country> findAllByOrderByCountryNameAsc();
}
