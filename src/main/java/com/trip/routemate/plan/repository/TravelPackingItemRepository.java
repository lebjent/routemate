package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelPackingItem;
import com.trip.routemate.plan.domain.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 여행 계획 준비물 목록을 정렬 순서로 조회하고 저장한다. */
public interface TravelPackingItemRepository extends JpaRepository<TravelPackingItem, Long> {
    List<TravelPackingItem> findByTravelPlanOrderBySortOrderAsc(TravelPlan travelPlan);
}
