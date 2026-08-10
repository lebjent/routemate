package com.trip.routemate.plan.controller;

import com.trip.routemate.plan.domain.TravelPlan;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import com.trip.routemate.user.repository.UserMstrRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-travel-plans")
public class TravelPlanController {

    private final TravelPlanRepository travelPlanRepository;
    private final UserMstrRepository userMstrRepository;

    @GetMapping
    public ResponseEntity<List<MyTravelPlanResponse>> getMyTravelPlans(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String userEmail = authentication.getName();
        String userNicknm = userMstrRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."))
                .getUserNicknm();

        List<MyTravelPlanResponse> plans = travelPlanRepository.findByUserNicknmOrderByMdfyDtDesc(userNicknm)
                .stream()
                .map(MyTravelPlanResponse::from)
                .toList();

        return ResponseEntity.ok(plans);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<MyTravelPlanResponse> createTravelPlan(
            Authentication authentication,
            @RequestBody CreateTravelPlanRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String userEmail = authentication.getName();
        String userNicknm = userMstrRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."))
                .getUserNicknm();

        String title = request.title() == null ? "" : request.title().trim();
        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일정 제목을 입력해 주세요.");
        }

        TravelPlan saved = travelPlanRepository.save(TravelPlan.builder()
                .userNicknm(userNicknm)
                .title(title)
                .description(request.description() == null ? null : request.description().trim())
                .imageUrl(request.imageUrl() == null || request.imageUrl().isBlank() ? null : request.imageUrl().trim())
                .spotCount(request.spotCount() == null ? 0 : Math.max(request.spotCount(), 0))
                .likeCount(0)
                .isPublic(request.isPublic() == null || request.isPublic().isBlank() ? "Y" : request.isPublic())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(MyTravelPlanResponse.from(saved));
    }

    public record MyTravelPlanResponse(
            Long planId,
            String title,
            String description,
            String imageUrl,
            String userNicknm,
            Integer spotCount,
            Integer likeCount,
            String isPublic,
            LocalDateTime createDt,
            LocalDateTime mdfyDt
    ) {
        static MyTravelPlanResponse from(TravelPlan plan) {
            return new MyTravelPlanResponse(
                    plan.getPlanId(),
                    plan.getTitle(),
                    plan.getDescription(),
                    plan.getImageUrl(),
                    plan.getUserNicknm(),
                    plan.getSpotCount(),
                    plan.getLikeCount(),
                    plan.getIsPublic(),
                    plan.getCreateDt(),
                    plan.getMdfyDt()
            );
        }
    }

    public record CreateTravelPlanRequest(
            @NotBlank String title,
            String description,
            String imageUrl,
            Integer spotCount,
            String isPublic
    ) {
    }
}
