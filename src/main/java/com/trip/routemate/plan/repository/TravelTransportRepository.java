package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelSchedule;
import com.trip.routemate.plan.domain.TravelTransport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

/** 세부 일정에 연결된 교통편 정보를 조회하고 저장한다. */
public interface TravelTransportRepository extends JpaRepository<TravelTransport, Long> {
    Optional<TravelTransport> findByTravelSchedule(TravelSchedule travelSchedule);

    List<TravelTransport> findByTravelScheduleIn(List<TravelSchedule> travelSchedules);
}
