package com.trip.routemate.user.controller;

import com.trip.routemate.user.dto.UserJoinDto;
import com.trip.routemate.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 회원가입과 가입 전 중복 확인을 제공하는 공개 API다.
 *
 * 중복 확인은 사용성 보조 기능일 뿐이며, 실제 가입 시 서비스 계층에서도 동일한 제약을 다시
 * 검증한다. 따라서 중복 확인 이후에도 가입 요청은 실패할 수 있다.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원가입 및 이메일·닉네임 중복 확인 API")
public class UserController {

    private final UserService userService;

    /**
     * 일반 회원 계정을 등록한다.
     *
     * @param dto 가입에 필요한 이메일, 비밀번호, 닉네임, 기본 회원 정보
     * @return 성공 메시지와 생성된 회원 식별자, 또는 중복 등 업무 오류 메시지
     */
    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임과 기본 회원 정보를 등록합니다.")
    public ResponseEntity<String> joinUser(@Valid @RequestBody UserJoinDto dto) {
        try {
            Long userId = userService.join(dto);
            return ResponseEntity.ok("회원가입 성공! 생성된 회원 번호: " + userId);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 닉네임이 이미 사용 중인지 확인한다.
     *
     * @param nicknm 확인할 닉네임
     * @return {@code true}이면 이미 사용 중인 닉네임
     */
    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인", description = "true이면 이미 사용 중인 닉네임입니다.")
    public ResponseEntity<Boolean> checkNickname(@RequestParam("nicknm") String nicknm) {
        boolean exists = userService.checkNicknameDuplicate(nicknm);
        return ResponseEntity.ok(exists);
    }

    /**
     * 이메일이 이미 가입되어 있는지 확인한다.
     *
     * @param email 확인할 이메일
     * @return {@code true}이면 이미 가입된 이메일
     */
    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인", description = "true이면 이미 가입된 이메일입니다.")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email) {
        boolean exists = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(exists);
    }
}
