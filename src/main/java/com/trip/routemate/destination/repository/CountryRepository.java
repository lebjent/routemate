package com.trip.routemate.destination.repository;

import com.trip.routemate.destination.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCountryCode(String countryCode);

    Optional<Country> findByCountryName(String countryName);

    List<Country> findAllByOrderByCountryNameAsc();
}
