package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelDay;
import com.trip.routemate.plan.domain.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 여행 계획에 속한 일차 정보를 날짜·일차 순서로 조회하고 저장한다. */
public interface TravelDayRepository extends JpaRepository<TravelDay, Long> {
    List<TravelDay> findByTravelPlanOrderByDayNumberAsc(TravelPlan travelPlan);
}
