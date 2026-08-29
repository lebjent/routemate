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
import com.trip.routemate.product.repository.ProductOrderRepository;
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
import java.util.Objects;

/**
 * 여행 계획의 소유권 검증과 생성·수정 트랜잭션을 담당한다.
 *
 * 계획의 하위 구조는 일차, 지역, 세부 일정, 교통편, 준비물로 이루어진다. 수정은 요청 전체를
 * 현재 구조로 교체하며, 연결한 예약 상품은 반드시 계획 소유자의 유효 예약이어야 한다.
 */
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
    private final ProductOrderRepository productOrderRepository;
    private final TravelPlanDetailAssembler travelPlanDetailAssembler;

    /** 로그인 사용자가 소유한 여행 계획 요약 목록을 조회한다. */
    public List<TravelPlanResponse> getMyTravelPlans(String userEmail) {
        var user = resolveActiveUser(userEmail);
        return travelPlanRepository.findByUser_UserIdOrderByMdfyDtDesc(user.getUserId())
                .stream()
                .map(TravelPlanResponse::from)
                .toList();
    }

    /** 사용자 소유권을 확인한 뒤 여행 계획 상세를 조합한다. */
    public TravelPlanDetailResponse getTravelPlan(String userEmail, Long planId) {
        var user = resolveActiveUser(userEmail);
        var plan = travelPlanRepository.findByPlanIdAndUser_UserId(planId, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행 일정을 찾을 수 없습니다."));

        return travelPlanDetailAssembler.assemble(plan);
    }

    /** 공개 상태인 여행 계획만 비로그인 사용자에게 상세로 제공하고 조회 수를 증가시킨다. */
    @Transactional
    public TravelPlanDetailResponse getPublicTravelPlan(Long planId) {
        if (travelPlanRepository.incrementPublicViewCount(planId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공개 여행 일정을 찾을 수 없습니다.");
        }

        var plan = travelPlanRepository.findByPlanIdAndIsPublic(planId, "Y")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공개 여행 일정을 찾을 수 없습니다."));
        return travelPlanDetailAssembler.assemble(plan);
    }

    /**
     * 새 여행 계획과 모든 하위 일정을 한 트랜잭션으로 저장한다.
     *
     * @param userEmail 계획 소유자의 로그인 이메일
     * @param request 여행 계획 전체 구조
     * @return 생성된 여행 계획 요약
     */
    @Transactional
    public TravelPlanResponse createTravelPlan(String userEmail, CreateTravelPlanRequest request) {
        var user = resolveActiveUser(userEmail);
        validateDays(request);

        var plan = createPlan(user, request);
        var spotCount = saveDays(plan, request.days(), user);
        savePackingItems(plan, request.packingItems());

        plan.updateSpotCount(spotCount);
        return TravelPlanResponse.from(plan);
    }

    /**
     * 소유자가 요청한 여행 계획 전체 구조로 기존 데이터를 교체한다.
     *
     * @param userEmail 계획 소유자의 로그인 이메일
     * @param planId 수정할 여행 계획 식별자
     * @param request 교체할 여행 계획 전체 구조
     * @return 수정된 여행 계획 요약
     */
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
        travelTransportRepository.deleteAll(Objects.requireNonNull(travelTransportRepository.findByTravelScheduleIn(oldSchedules)));
        travelTransportRepository.flush();
        travelScheduleRepository.deleteAll(Objects.requireNonNull(oldSchedules));
        travelScheduleRepository.flush();
        travelDayRegionRepository.deleteAll(Objects.requireNonNull(oldRegions));
        travelDayRegionRepository.flush();
        travelDayRepository.deleteAll(Objects.requireNonNull(oldDays));
        travelDayRepository.flush();
        travelPackingItemRepository.deleteAll(Objects.requireNonNull(travelPackingItemRepository.findByTravelPlanOrderBySortOrderAsc(plan)));
        travelPackingItemRepository.flush();

        var spotCount = saveDays(plan, request.days(), user);
        savePackingItems(plan, request.packingItems());
        plan.updateSpotCount(spotCount);
        return TravelPlanResponse.from(plan);
    }

    private TravelPlan createPlan(UserMstr user, CreateTravelPlanRequest request) {
        return travelPlanRepository.save(Objects.requireNonNull(TravelPlan.builder()
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
                .build()));
    }

    private int saveDays(TravelPlan plan, List<TravelDayRequest> dayRequests, UserMstr user) {
        var spotCount = 0;
        for (var dayRequest : dayRequests == null ? List.<TravelDayRequest>of() : dayRequests) {
            spotCount += saveDay(plan, dayRequest, user);
        }
        return spotCount;
    }

    private int saveDay(TravelPlan plan, TravelDayRequest dayRequest, UserMstr user) {
        var day = travelDayRepository.save(Objects.requireNonNull(TravelDay.builder()
                .travelPlan(plan)
                .dayNumber(dayRequest.dayNumber())
                .planDate(dayRequest.planDate())
                .build()));
        var spotCount = 0;
        for (var regionIndex = 0; regionIndex < dayRequest.regions().size(); regionIndex++) {
            spotCount += saveDayRegion(day, dayRequest.regions().get(regionIndex), regionIndex + 1, user);
        }
        return spotCount;
    }

    private int saveDayRegion(TravelDay day, TravelDayRegionRequest request, int sortOrder, UserMstr user) {
        var country = countryRepository.findByCountryCode(request.countryCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 국가를 찾을 수 없습니다."));
        var region = regionRepository.findByCountryAndRegionCode(country, request.regionCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 지역을 찾을 수 없습니다."));
        var dayRegion = travelDayRegionRepository.save(Objects.requireNonNull(TravelDayRegion.builder()
                .travelDay(day)
                .country(country)
                .region(region)
                .regionNote(normalizeOptionalText(request.note()))
                .sortOrder(sortOrder)
                .build()));
        return saveSchedules(dayRegion, request.schedules(), user);
    }

    private int saveSchedules(TravelDayRegion dayRegion, List<TravelScheduleRequest> scheduleRequests, UserMstr user) {
        var spotCount = 0;
        for (var scheduleIndex = 0; scheduleIndex < scheduleRequests.size(); scheduleIndex++) {
            var request = scheduleRequests.get(scheduleIndex);
            if (hasScheduleContent(request)) {
                saveSchedule(dayRegion, request, scheduleIndex + 1, user);
                spotCount++;
            }
        }
        return spotCount;
    }

    private void saveSchedule(TravelDayRegion dayRegion, TravelScheduleRequest request, int sortOrder, UserMstr user) {
        var productOrder = resolveProductOrder(request.productOrderId(), user);
        var schedule = travelScheduleRepository.save(Objects.requireNonNull(TravelSchedule.builder()
                .travelDayRegion(dayRegion)
                .productOrder(productOrder)
                .scheduleTime(normalizeOptionalText(request.time()))
                .title(normalizeOptionalText(request.title()))
                .location(normalizeOptionalText(request.location()))
                .memo(normalizeOptionalText(request.memo()))
                .sortOrder(sortOrder)
                .build()));
        saveTransport(schedule, request);
    }

    private void saveTransport(TravelSchedule schedule, TravelScheduleRequest request) {
        var transportType = normalizeTransportType(request.transportType());
        if (transportType == null) {
            return;
        }
        travelTransportRepository.save(Objects.requireNonNull(TravelTransport.builder()
                .travelSchedule(schedule)
                .transportType(transportType)
                .transportName(normalizeOptionalText(request.transportName()))
                .departureTime(normalizeOptionalText(request.departureTime()))
                .arrivalTime(normalizeOptionalText(request.arrivalTime()))
                .transportMemo(normalizeOptionalText(request.transportMemo()))
                .build()));
    }

    private void savePackingItems(TravelPlan plan, List<PackingItemRequest> packingItems) {
        var requests = packingItems == null ? List.<PackingItemRequest>of() : packingItems;
        for (var index = 0; index < requests.size(); index++) {
            var item = requests.get(index);
            var itemName = normalizeOptionalText(item.item());
            if (itemName == null) {
                continue;
            }
            travelPackingItemRepository.save(Objects.requireNonNull(TravelPackingItem.builder()
                    .travelPlan(plan)
                    .itemName(itemName)
                    .requiredYn(Boolean.FALSE.equals(item.required()) ? "N" : "Y")
                    .sortOrder(index + 1)
                    .build()));
        }
    }

    private void validateDays(CreateTravelPlanRequest request) {
        if (request.travelEndDate().isBefore(request.travelStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.");
        }

        var expectedDays = request.travelStartDate().datesUntil(request.travelEndDate().plusDays(1)).toList();
        var days = request.days() == null ? List.<TravelDayRequest>of() : new ArrayList<>(request.days());
        days.sort(Comparator.comparing((TravelDayRequest day) -> day.dayNumber()));
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
                || normalizeTransportType(request.transportType()) != null
                || request.productOrderId() != null;
    }

    private com.trip.routemate.product.domain.ProductOrder resolveProductOrder(Long productOrderId, UserMstr user) {
        if (productOrderId == null) {
            return null;
        }
        return productOrderRepository.findById(productOrderId)
                .filter(order -> order.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인이 예약한 옵션상품만 일정에 연결할 수 있습니다."));
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
        var normalized = value == null ? "" : value.trim();
        return switch (normalized) {
            case "TRAIN", "CAR", "FLIGHT", "CRUISE", "OTHER" -> normalized;
            default -> null;
        };
    }
}
