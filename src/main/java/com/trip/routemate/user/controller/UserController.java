package com.trip.routemate.user.controller;

import com.trip.routemate.user.dto.UserJoinDto;
import com.trip.routemate.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API 엔드포인트
     * POST http://localhost:8090/api/user/join
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinUser(@RequestBody UserJoinDto dto) {
        try {
            // 서비스 호출하여 가입 진행 후 생성된 PK(USER_ID) 리턴받음
            Long userId = userService.join(dto);
            return ResponseEntity.ok("회원가입 성공! 생성된 회원 번호: " + userId);
        } catch (IllegalStateException e) {
            // 중복 이메일이나 비밀번호 불일치 등 비즈니스 예외 발생 시 400 Bad Request 에러 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 닉네임 중복 체크 API
     * GET http://localhost:8090/api/user/check-nickname?nicknm=...
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam("nicknm") String nicknm) {
        boolean exists = userService.checkNicknameDuplicate(nicknm);
        return ResponseEntity.ok(exists);
    }

    /**
     * 이메일 중복 체크 API
     * GET http://localhost:8090/api/user/check-email?email=...
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email) {
        boolean exists = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(exists);
    }
}
