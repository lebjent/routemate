package com.trip.routemate.destination.repository;

import com.trip.routemate.destination.domain.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/** 여행지 마스터 및 홈·관리 화면용 조회를 담당한다. */
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    /**
     * 좋아요(인기) 순으로 정렬하여 상위 3개의 여행지를 조회합니다.
     */
    @EntityGraph(attributePaths = {"country", "region"})
    List<Destination> findTop3ByOrderByLikeCountDesc();

    @EntityGraph(attributePaths = {"country", "region"})
    List<Destination> findTop5ByOrderByLikeCountDesc();

    List<Destination> findAllByOrderByLikeCountDesc();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"country", "region"})
    List<Destination> findAllByOrderByDestNameAsc();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"country", "region"})
    Optional<Destination> findWithCountryAndRegionByDestId(Long destId);

    Optional<Destination> findTopByCountryCountryIdAndRegionRegionIdOrderByLikeCountDesc(Long countryId, Long regionId);
}
