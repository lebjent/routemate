package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelDay;
import com.trip.routemate.plan.domain.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelDayRepository extends JpaRepository<TravelDay, Long> {
    List<TravelDay> findByTravelPlanOrderByDayNumberAsc(TravelPlan travelPlan);
}
