package com.trip.routemate.plan.service;

import com.trip.routemate.plan.domain.TravelDay;
import com.trip.routemate.plan.domain.TravelDayRegion;
import com.trip.routemate.plan.domain.TravelPlan;
import com.trip.routemate.plan.domain.TravelSchedule;
import com.trip.routemate.plan.domain.TravelTransport;
import com.trip.routemate.plan.dto.TravelPlanDetailResponse;
import com.trip.routemate.plan.repository.TravelDayRegionRepository;
import com.trip.routemate.plan.repository.TravelDayRepository;
import com.trip.routemate.plan.repository.TravelPackingItemRepository;
import com.trip.routemate.plan.repository.TravelScheduleRepository;
import com.trip.routemate.plan.repository.TravelTransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 분산 저장된 여행 일정 엔티티를 화면용 상세 응답 하나로 조합한다.
 *
 * 일차, 방문 지역, 세부 일정, 교통편, 준비물은 별도 테이블에 있으므로 이 클래스에 변환 규칙을
 * 모아 서비스의 생성·수정 로직과 조회 조립 로직을 분리한다.
 */
@Component
@RequiredArgsConstructor
public class TravelPlanDetailAssembler {

    private final TravelDayRepository travelDayRepository;
    private final TravelDayRegionRepository travelDayRegionRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelTransportRepository travelTransportRepository;
    private final TravelPackingItemRepository travelPackingItemRepository;

    /**
     * 여행 계획에 속한 모든 세부 데이터를 정렬 순서대로 조합한다.
     *
     * @param plan 상세 조회가 허용된 여행 계획 엔티티
     * @return 일차·지역·일정·교통편·준비물을 포함한 상세 응답
     */
    public TravelPlanDetailResponse assemble(TravelPlan plan) {
        var days = travelDayRepository.findByTravelPlanOrderByDayNumberAsc(plan);
        var dayRegions = days.isEmpty()
                ? List.<TravelDayRegion>of()
                : travelDayRegionRepository.findByTravelDayInOrderBySortOrderAsc(days);
        var schedules = dayRegions.isEmpty()
                ? List.<TravelSchedule>of()
                : travelScheduleRepository.findByTravelDayRegionInOrderBySortOrderAsc(dayRegions);
        var transportsByScheduleId = travelTransportRepository.findByTravelScheduleIn(schedules).stream()
                .collect(Collectors.toMap(transport -> transport.getTravelSchedule().getScheduleId(), Function.identity()));
        var regionsByDayId = dayRegions.stream().collect(Collectors.groupingBy(region -> region.getTravelDay().getDayId()));
        var schedulesByRegionId = schedules.stream().collect(Collectors.groupingBy(schedule -> schedule.getTravelDayRegion().getDayRegionId()));

        return new TravelPlanDetailResponse(
                plan.getPlanId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getImageUrl(),
                plan.getUserNicknm(),
                plan.getSpotCount(),
                plan.getViewCount(),
                plan.getIsPublic(),
                plan.getTravelStartDate(),
                plan.getTravelEndDate(),
                plan.getCreateDt(),
                days.stream().map(day -> toDayResponse(day, regionsByDayId, schedulesByRegionId, transportsByScheduleId)).toList(),
                travelPackingItemRepository.findByTravelPlanOrderBySortOrderAsc(plan).stream()
                        .map(item -> new TravelPlanDetailResponse.PackingItem(item.getItemName(), "Y".equals(item.getRequiredYn())))
                        .toList()
        );
    }

    /** 한 일차와 하위 지역·일정을 상세 응답의 일차 항목으로 변환한다. */
    private TravelPlanDetailResponse.Day toDayResponse(
            TravelDay day,
            Map<Long, List<TravelDayRegion>> regionsByDayId,
            Map<Long, List<TravelSchedule>> schedulesByRegionId,
            Map<Long, TravelTransport> transportsByScheduleId
    ) {
        var regions = regionsByDayId.getOrDefault(day.getDayId(), List.of()).stream()
                .map(region -> toRegionResponse(region, schedulesByRegionId, transportsByScheduleId))
                .toList();
        return new TravelPlanDetailResponse.Day(day.getDayNumber(), day.getPlanDate(), regions);
    }

    /** 일차 지역과 해당 지역의 세부 일정을 응답 항목으로 변환한다. */
    private TravelPlanDetailResponse.Region toRegionResponse(
            TravelDayRegion dayRegion,
            Map<Long, List<TravelSchedule>> schedulesByRegionId,
            Map<Long, TravelTransport> transportsByScheduleId
    ) {
        var schedules = schedulesByRegionId.getOrDefault(dayRegion.getDayRegionId(), List.of()).stream()
                .map(schedule -> toScheduleResponse(schedule, transportsByScheduleId.get(schedule.getScheduleId())))
                .toList();
        return new TravelPlanDetailResponse.Region(
                dayRegion.getCountry().getCountryCode(),
                dayRegion.getCountry().getCountryName(),
                dayRegion.getRegion().getRegionCode(),
                dayRegion.getRegion().getRegionName(),
                dayRegion.getRegionNote(),
                schedules
        );
    }

    /** 세부 일정과 선택 교통편을 하나의 일정 응답으로 변환한다. */
    private TravelPlanDetailResponse.Schedule toScheduleResponse(TravelSchedule schedule, TravelTransport transport) {
        return new TravelPlanDetailResponse.Schedule(
                schedule.getScheduleTime(),
                schedule.getTitle(),
                schedule.getLocation(),
                schedule.getMemo(),
                transport == null ? null : transport.getTransportType(),
                transport == null ? null : transport.getTransportName(),
                transport == null ? null : transport.getDepartureTime(),
                transport == null ? null : transport.getArrivalTime(),
                transport == null ? null : transport.getTransportMemo(),
                schedule.getProductOrder() == null ? null : schedule.getProductOrder().getOrderId(),
                schedule.getProductOrder() == null ? null : schedule.getProductOrder().getOrderNo()
        );
    }
}
