package com.trip.routemate.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 🔥 API가 아닌 HTML 화면을 리턴하는 컨트롤러입니다.
public class UserViewController {

    /**
     * 회원가입 화면 이동
     * 브라우저에 http://localhost:8090/join 을 치면 이 메서드가 낚아챕니다.
     */
    @GetMapping("/join")
    public String joinPage() {
        // src/main/resources/templates/user/join.html 파일을 찾아가라!
        return "user/join";
    }

    // 🔥 로그인 화면 이동 메서드 추가!
    @GetMapping("/login")
    public String loginPage() {
        // src/main/resources/templates/user/login.html 파일을 찾아갑니다.
        return "user/login";
    }
}