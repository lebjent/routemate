package com.trip.routemate.plan.service;

import com.trip.routemate.plan.dto.CreateTravelPlanRequest;
import com.trip.routemate.plan.dto.TravelPlanResponse;
import com.trip.routemate.plan.dto.TravelPlanDetailResponse;

import java.util.List;

public interface TravelPlanService {
    List<TravelPlanResponse> getMyTravelPlans(String userEmail);

    TravelPlanDetailResponse getTravelPlan(String userEmail, Long planId);

    TravelPlanResponse createTravelPlan(String userEmail, CreateTravelPlanRequest request);
}
