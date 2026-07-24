package com.trip.routemate.global.cotroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
        "/login",
        "/join",
        "/lotto"
    })
    public String forward() {
        // static/index.html로 포워딩하여 리액트 라우터가 처리하게 함
        return "forward:/index.html";
    }
}
