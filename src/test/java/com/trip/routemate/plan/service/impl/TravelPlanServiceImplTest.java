package com.trip.routemate.plan.service.impl;

import com.trip.routemate.destination.repository.CountryRepository;
import com.trip.routemate.destination.repository.RegionRepository;
import com.trip.routemate.plan.dto.CreateTravelPlanRequest;
import com.trip.routemate.plan.repository.TravelDayRegionRepository;
import com.trip.routemate.plan.repository.TravelDayRepository;
import com.trip.routemate.plan.repository.TravelPackingItemRepository;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import com.trip.routemate.plan.repository.TravelScheduleRepository;
import com.trip.routemate.plan.repository.TravelTransportRepository;
import com.trip.routemate.plan.service.TravelPlanDetailAssembler;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelPlanServiceImplTest {

    private static final String USER_EMAIL = "traveler@example.com";

    @Mock private TravelPlanRepository travelPlanRepository;
    @Mock private TravelDayRepository travelDayRepository;
    @Mock private TravelDayRegionRepository travelDayRegionRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private TravelTransportRepository travelTransportRepository;
    @Mock private TravelPackingItemRepository travelPackingItemRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private UserMstrRepository userMstrRepository;
    @Mock private TravelPlanDetailAssembler travelPlanDetailAssembler;

    @InjectMocks private TravelPlanServiceImpl travelPlanService;

    @Test
    void createTravelPlan_rejectsEndDateBeforeStartDateBeforeSaving() {
        when(userMstrRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(activeUser()));
        var request = new CreateTravelPlanRequest(
                "여름 휴가",
                null,
                null,
                "Y",
                LocalDate.of(2026, 6, 5),
                LocalDate.of(2026, 6, 1),
                List.of(),
                List.of()
        );

        assertThatThrownBy(() -> travelPlanService.createTravelPlan(USER_EMAIL, request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(travelPlanRepository, never()).save(any());
    }

    @Test
    void getTravelPlan_doesNotExposeAnotherUsersPlan() {
        when(userMstrRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(activeUser()));
        when(travelPlanRepository.findByPlanIdAndUser_UserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> travelPlanService.getTravelPlan(USER_EMAIL, 99L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(travelPlanDetailAssembler, never()).assemble(any());
    }

    private UserMstr activeUser() {
        return UserMstr.builder()
                .userId(1L)
                .userEmail(USER_EMAIL)
                .userNicknm("여행자")
                .build();
    }
}
