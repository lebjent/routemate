package com.trip.routemate.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
        "/login",
        "/admin/login",
        "/admin",
        "/admin/users",
        "/admin/staff",
        "/admin/destinations",
        "/admin/recommendations",
        "/join",
        "/lotto",
        "/my-trips",
        "/my-trips/{planId}",
        "/my-trips/{planId}/edit",
        "/travel-plans/{planId}",
        "/products",
        "/products/{productId}",
        "/my-product-orders"
    })
    public String forward() {
        // static/index.html로 포워딩하여 리액트 라우터가 처리하게 함
        return "forward:/index.html";
    }
}
