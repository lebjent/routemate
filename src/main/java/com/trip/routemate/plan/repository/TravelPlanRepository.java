package com.trip.routemate.plan.repository;

import com.trip.routemate.plan.domain.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    List<TravelPlan> findTop5ByIsPublicOrderByViewCountDescPlanIdDesc(String isPublic);

    List<TravelPlan> findTop5ByOrderByCreateDtDesc();

    long countByIsPublic(String isPublic);

    @Query("select coalesce(sum(plan.viewCount), 0) from TravelPlan plan")
    Long getTotalViewCount();

    Optional<TravelPlan> findByPlanIdAndIsPublic(Long planId, String isPublic);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update TravelPlan plan
               set plan.viewCount = plan.viewCount + 1
             where plan.planId = :planId
               and plan.isPublic = 'Y'
            """)
    int incrementPublicViewCount(@Param("planId") Long planId);

    /**
     * 로그인한 사용자의 여행 일정을 수정일 최신순으로 조회합니다.
     */
    List<TravelPlan> findByUser_UserIdOrderByMdfyDtDesc(Long userId);

    Optional<TravelPlan> findByPlanIdAndUser_UserId(Long planId, Long userId);
}
