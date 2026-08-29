package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelDay;
import com.trip.routemate.plan.domain.TravelDayRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 여행 일차에 속한 방문 지역을 정렬 순서로 조회하고 저장한다. */
public interface TravelDayRegionRepository extends JpaRepository<TravelDayRegion, Long> {
    List<TravelDayRegion> findByTravelDayOrderBySortOrderAsc(TravelDay travelDay);

    List<TravelDayRegion> findByTravelDayInOrderBySortOrderAsc(List<TravelDay> travelDays);
}
