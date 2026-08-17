package com.trip.routemate.destination.repository;

import com.trip.routemate.destination.domain.Destination;
import com.trip.routemate.destination.domain.DestinationRecommendation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DestinationRecommendationRepository extends JpaRepository<DestinationRecommendation, Long> {
    @Query("""
            select recommendation.destination
              from DestinationRecommendation recommendation
             where recommendation.useYn = 'Y'
               and recommendation.displayStartDt <= :now
               and recommendation.displayEndDt >= :now
             order by recommendation.sortOrder asc, recommendation.destination.likeCount desc, recommendation.recommendId desc
            """)
    List<Destination> findActiveDestinations(@Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"destination", "destination.country", "destination.region"})
    List<DestinationRecommendation> findAllByOrderBySortOrderAscDisplayStartDtDesc();

    @EntityGraph(attributePaths = {"destination", "destination.country", "destination.region"})
    Optional<DestinationRecommendation> findWithDestinationByRecommendId(Long recommendId);

    boolean existsByDestinationAndDisplayStartDtLessThanEqualAndDisplayEndDtGreaterThanEqual(Destination destination, LocalDateTime end, LocalDateTime start);
}
