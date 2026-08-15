package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelDay;
import com.trip.routemate.plan.domain.TravelDayRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelDayRegionRepository extends JpaRepository<TravelDayRegion, Long> {
    List<TravelDayRegion> findByTravelDayOrderBySortOrderAsc(TravelDay travelDay);

    List<TravelDayRegion> findByTravelDayInOrderBySortOrderAsc(List<TravelDay> travelDays);
}
