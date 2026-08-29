package com.trip.routemate.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * React 단일 페이지 애플리케이션의 클라이언트 라우트를 진입 HTML로 전달한다.
 *
 * API 요청은 이 컨트롤러를 거치지 않고 각 REST 컨트롤러가 직접 처리한다.
 */
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
    /** React Router가 화면별 라우팅을 처리하도록 정적 진입 페이지로 포워딩한다. */
    public String forward() {
        return "forward:/index.html";
    }
}
