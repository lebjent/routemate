package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    /**
     * 공개 상태인 일정 중 좋아요(인기) 순으로 정렬하여 상위 3개를 조회합니다.
     */
    List<TravelPlan> findTop3ByIsPublicOrderByLikeCountDesc(String isPublic);

    /**
     * 로그인한 사용자의 여행 일정을 수정일 최신순으로 조회합니다.
     */
    List<TravelPlan> findByUser_UserIdOrderByMdfyDtDesc(Long userId);

    Optional<TravelPlan> findByPlanIdAndUser_UserId(Long planId, Long userId);
}
