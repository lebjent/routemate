package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelDayRegion;
import com.trip.routemate.plan.domain.TravelSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 방문 지역에 속한 세부 일정을 정렬 순서로 조회하고 저장한다. */
public interface TravelScheduleRepository extends JpaRepository<TravelSchedule, Long> {
    List<TravelSchedule> findByTravelDayRegionOrderBySortOrderAsc(TravelDayRegion travelDayRegion);

    List<TravelSchedule> findByTravelDayRegionInOrderBySortOrderAsc(List<TravelDayRegion> dayRegions);
}
