package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelSchedule;
import com.trip.routemate.plan.domain.TravelTransport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TravelTransportRepository extends JpaRepository<TravelTransport, Long> {
    Optional<TravelTransport> findByTravelSchedule(TravelSchedule travelSchedule);

    List<TravelTransport> findByTravelScheduleIn(List<TravelSchedule> travelSchedules);
}
