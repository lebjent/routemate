package com.trip.routemate.global.cotroller;

import com.trip.routemate.destination.repository.DestinationRepository;
import com.trip.routemate.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DestinationRepository destinationRepository;
    private final TravelPlanRepository travelPlanRepository;

    /**
     * 메인 화면 (Home)
     * GET http://localhost:8090/
     */
    @GetMapping("/")
    public String home(Model model) {
        // 데이터베이스에서 상위 3개 인기 여행지와 상위 3개 인기 일정을 조회하여 모델에 추가합니다.
        model.addAttribute("destinations", destinationRepository.findTop3ByOrderByLikeCountDesc());
        model.addAttribute("plans", travelPlanRepository.findTop3ByIsPublicOrderByLikeCountDesc("Y"));
        return "index"; // src/main/resources/templates/index.html 파일을 찾아갑니다.
    }

}

