package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelPackingItem;
import com.trip.routemate.plan.domain.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPackingItemRepository extends JpaRepository<TravelPackingItem, Long> {
    List<TravelPackingItem> findByTravelPlanOrderBySortOrderAsc(TravelPlan travelPlan);
}
