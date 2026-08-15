package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelDayRegion;
import com.trip.routemate.plan.domain.TravelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelScheduleRepository extends JpaRepository<TravelSchedule, Long> {
    List<TravelSchedule> findByTravelDayRegionOrderBySortOrderAsc(TravelDayRegion travelDayRegion);

    List<TravelSchedule> findByTravelDayRegionInOrderBySortOrderAsc(List<TravelDayRegion> dayRegions);
}
