package com.trip.routemate.plan.service;

import com.trip.routemate.destination.repository.CountryRepository;
import com.trip.routemate.destination.repository.RegionRepository;
import com.trip.routemate.plan.domain.TravelDay;
import com.trip.routemate.plan.domain.TravelDayRegion;
import com.trip.routemate.plan.domain.TravelPackingItem;
import com.trip.routemate.plan.domain.TravelPlan;
import com.trip.routemate.plan.domain.TravelSchedule;
import com.trip.routemate.plan.domain.TravelTransport;
import com.trip.routemate.plan.dto.CreateTravelPlanRequest;
import com.trip.routemate.plan.dto.PackingItemRequest;
import com.trip.routemate.plan.dto.TravelDayRegionRequest;
import com.trip.routemate.plan.dto.TravelDayRequest;
import com.trip.routemate.plan.dto.TravelPlanDetailResponse;
import com.trip.routemate.plan.dto.TravelPlanResponse;
import com.trip.routemate.plan.dto.TravelScheduleRequest;
import com.trip.routemate.plan.repository.TravelDayRegionRepository;
import com.trip.routemate.plan.repository.TravelDayRepository;
import com.trip.routemate.plan.repository.TravelPackingItemRepository;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import com.trip.routemate.plan.repository.TravelScheduleRepository;
import com.trip.routemate.plan.repository.TravelTransportRepository;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final TravelDayRepository travelDayRepository;
    private final TravelDayRegionRepository travelDayRegionRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelTransportRepository travelTransportRepository;
    private final TravelPackingItemRepository travelPackingItemRepository;
    private final CountryRepository countryRepository;
    private final RegionRepository regionRepository;
    private final UserMstrRepository userMstrRepository;
    private final TravelPlanDetailAssembler travelPlanDetailAssembler;

    public List<TravelPlanResponse> getMyTravelPlans(String userEmail) {
        var user = resolveActiveUser(userEmail);
        return travelPlanRepository.findByUser_UserIdOrderByMdfyDtDesc(user.getUserId())
                .stream()
                .map(TravelPlanResponse::from)
                .toList();
    }

    public TravelPlanDetailResponse getTravelPlan(String userEmail, Long planId) {
        var user = resolveActiveUser(userEmail);
        var plan = travelPlanRepository.findByPlanIdAndUser_UserId(planId, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행 일정을 찾을 수 없습니다."));

        return travelPlanDetailAssembler.assemble(plan);
    }

    @Transactional
    public TravelPlanDetailResponse getPublicTravelPlan(Long planId) {
        if (travelPlanRepository.incrementPublicViewCount(planId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개 여행 일정을 찾을 수 없습니다.");
        }

        var plan = travelPlanRepository.findByPlanIdAndIsPublic(planId, "Y")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공개 여행 일정을 찾을 수 없습니다."));
        return travelPlanDetailAssembler.assemble(plan);
    }

    @Transactional
    public TravelPlanResponse createTravelPlan(String userEmail, CreateTravelPlanRequest request) {
        var user = resolveActiveUser(userEmail);
        validateDays(request);

        var plan = createPlan(user, request);
        var spotCount = saveDays(plan, request.days());
        savePackingItems(plan, request.packingItems());

        plan.updateSpotCount(spotCount);
        return TravelPlanResponse.from(plan);
    }

    @Transactional
    public TravelPlanResponse updateTravelPlan(String userEmail, Long planId, CreateTravelPlanRequest request) {
        var user = resolveActiveUser(userEmail);
        validateDays(request);
        var plan = travelPlanRepository.findByPlanIdAndUser_UserId(planId, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행 일정을 찾을 수 없습니다."));
        plan.updateDetails(normalizeRequiredText(request.title(), "일정 제목을 입력해 주세요."), normalizeOptionalText(request.description()),
                normalizeOptionalText(request.imageUrl()), request.travelStartDate(), request.travelEndDate(), normalizePublicFlag(request.isPublic()));

        var oldDays = travelDayRepository.findByTravelPlanOrderByDayNumberAsc(plan);
        var oldRegions = travelDayRegionRepository.findByTravelDayInOrderBySortOrderAsc(oldDays);
        var oldSchedules = travelScheduleRepository.findByTravelDayRegionInOrderBySortOrderAsc(oldRegions);
        travelTransportRepository.deleteAll(travelTransportRepository.findByTravelScheduleIn(oldSchedules));
        travelTransportRepository.flush();
        travelScheduleRepository.deleteAll(oldSchedules);
        travelScheduleRepository.flush();
        travelDayRegionRepository.deleteAll(oldRegions);
        travelDayRegionRepository.flush();
        travelDayRepository.deleteAll(oldDays);
        travelDayRepository.flush();
        travelPackingItemRepository.deleteAll(travelPackingItemRepository.findByTravelPlanOrderBySortOrderAsc(plan));
        travelPackingItemRepository.flush();

        var spotCount = saveDays(plan, request.days());
        savePackingItems(plan, request.packingItems());
        plan.updateSpotCount(spotCount);
        return TravelPlanResponse.from(plan);
    }

    private TravelPlan createPlan(UserMstr user, CreateTravelPlanRequest request) {
        return travelPlanRepository.save(TravelPlan.builder()
                .user(user)
                .userNicknm(user.getUserNicknm())
                .title(normalizeRequiredText(request.title(), "일정 제목을 입력해 주세요."))
                .description(normalizeOptionalText(request.description()))
                .imageUrl(normalizeOptionalText(request.imageUrl()))
                .travelStartDate(request.travelStartDate())
                .travelEndDate(request.travelEndDate())
                .spotCount(0)
                .likeCount(0)
                .viewCount(0L)
                .isPublic(normalizePublicFlag(request.isPublic()))
                .build());
    }

    private int saveDays(TravelPlan plan, List<TravelDayRequest> dayRequests) {
        var spotCount = 0;
        for (var dayRequest : dayRequests == null ? List.<TravelDayRequest>of() : dayRequests) {
            spotCount += saveDay(plan, dayRequest);
        }
        return spotCount;
    }

    private int saveDay(TravelPlan plan, TravelDayRequest dayRequest) {
        var day = travelDayRepository.save(TravelDay.builder()
                .travelPlan(plan)
                .dayNumber(dayRequest.dayNumber())
                .planDate(dayRequest.planDate())
                .build());
        var spotCount = 0;
        for (var regionIndex = 0; regionIndex < dayRequest.regions().size(); regionIndex++) {
            spotCount += saveDayRegion(day, dayRequest.regions().get(regionIndex), regionIndex + 1);
        }
        return spotCount;
    }

    private int saveDayRegion(TravelDay day, TravelDayRegionRequest request, int sortOrder) {
        var country = countryRepository.findByCountryCode(request.countryCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 국가를 찾을 수 없습니다."));
        var region = regionRepository.findByCountryAndRegionCode(country, request.regionCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 지역을 찾을 수 없습니다."));
        var dayRegion = travelDayRegionRepository.save(TravelDayRegion.builder()
                .travelDay(day)
                .country(country)
                .region(region)
                .regionNote(normalizeOptionalText(request.note()))
                .sortOrder(sortOrder)
                .build());
        return saveSchedules(dayRegion, request.schedules());
    }

    private int saveSchedules(TravelDayRegion dayRegion, List<TravelScheduleRequest> scheduleRequests) {
        var spotCount = 0;
        for (var scheduleIndex = 0; scheduleIndex < scheduleRequests.size(); scheduleIndex++) {
            var request = scheduleRequests.get(scheduleIndex);
            if (hasScheduleContent(request)) {
                saveSchedule(dayRegion, request, scheduleIndex + 1);
                spotCount++;
            }
        }
        return spotCount;
    }

    private void saveSchedule(TravelDayRegion dayRegion, TravelScheduleRequest request, int sortOrder) {
        var schedule = travelScheduleRepository.save(TravelSchedule.builder()
                .travelDayRegion(dayRegion)
                .scheduleTime(normalizeOptionalText(request.time()))
                .title(normalizeOptionalText(request.title()))
                .location(normalizeOptionalText(request.location()))
                .memo(normalizeOptionalText(request.memo()))
                .sortOrder(sortOrder)
                .build());
        saveTransport(schedule, request);
    }

    private void saveTransport(TravelSchedule schedule, TravelScheduleRequest request) {
        var transportType = normalizeTransportType(request.transportType());
        if (transportType == null) {
            return;
        }
        travelTransportRepository.save(TravelTransport.builder()
                .travelSchedule(schedule)
                .transportType(transportType)
                .transportName(normalizeOptionalText(request.transportName()))
                .departureTime(normalizeOptionalText(request.departureTime()))
                .arrivalTime(normalizeOptionalText(request.arrivalTime()))
                .transportMemo(normalizeOptionalText(request.transportMemo()))
                .build());
    }

    private void savePackingItems(TravelPlan plan, List<PackingItemRequest> packingItems) {
        var requests = packingItems == null ? List.<PackingItemRequest>of() : packingItems;
        for (var index = 0; index < requests.size(); index++) {
            var item = requests.get(index);
            var itemName = normalizeOptionalText(item.item());
            if (itemName == null) {
                continue;
            }
            travelPackingItemRepository.save(TravelPackingItem.builder()
                    .travelPlan(plan)
                    .itemName(itemName)
                    .requiredYn(Boolean.FALSE.equals(item.required()) ? "N" : "Y")
                    .sortOrder(index + 1)
                    .build());
        }
    }

    private void validateDays(CreateTravelPlanRequest request) {
        if (request.travelEndDate().isBefore(request.travelStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.");
        }

        var expectedDays = request.travelStartDate().datesUntil(request.travelEndDate().plusDays(1)).toList();
        var days = request.days() == null ? List.<TravelDayRequest>of() : new ArrayList<>(request.days());
        days.sort(Comparator.comparing(TravelDayRequest::dayNumber));
        for (var day : days) {
            if (day.dayNumber() < 1 || day.dayNumber() > expectedDays.size()
                    || !expectedDays.contains(day.planDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일차와 날짜 정보가 올바르지 않습니다.");
            }
            if (day.regions() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "여행지 정보가 올바르지 않습니다.");
            }
        }

    }

    private boolean hasScheduleContent(TravelScheduleRequest request) {
        return normalizeOptionalText(request.title()) != null
                || normalizeOptionalText(request.location()) != null
                || normalizeOptionalText(request.memo()) != null
                || normalizeTransportType(request.transportType()) != null;
    }

    private UserMstr resolveActiveUser(String userEmail) {
        return userMstrRepository.findByUserEmail(userEmail)
                .filter(user -> "ACTIVE".equals(user.getUserStatCd()))
                .filter(user -> "N".equals(user.getDelYn()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    private String normalizeRequiredText(String value, String message) {
        var normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizePublicFlag(String value) {
        return "N".equals(value) ? "N" : "Y";
    }

    private String normalizeTransportType(String value) {
        return switch (value == null ? "" : value.trim()) {
            case "TRAIN", "CAR", "FLIGHT", "CRUISE", "OTHER" -> value.trim();
            default -> null;
        };
    }
}
