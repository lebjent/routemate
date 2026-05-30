package com.trip.routemate.global.cotroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * 메인 화면 (Home)
     * GET http://localhost:8090/
     */
    @GetMapping("/")
    public String home() {
        return "index"; // src/main/resources/templates/index.html 파일을 찾아갑니다.
    }

}
