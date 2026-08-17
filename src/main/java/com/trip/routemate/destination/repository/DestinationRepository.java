package com.trip.routemate.destination.repository;

import com.trip.routemate.destination.domain.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    /**
     * 좋아요(인기) 순으로 정렬하여 상위 3개의 여행지를 조회합니다.
     */
    List<Destination> findTop3ByOrderByLikeCountDesc();

    List<Destination> findTop5ByOrderByLikeCountDesc();

    List<Destination> findAllByOrderByLikeCountDesc();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"country", "region"})
    List<Destination> findAllByOrderByDestNameAsc();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"country", "region"})
    Optional<Destination> findWithCountryAndRegionByDestId(Long destId);

    Optional<Destination> findTopByCountryCountryIdAndRegionRegionIdOrderByLikeCountDesc(Long countryId, Long regionId);
}
