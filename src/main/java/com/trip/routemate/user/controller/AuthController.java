package com.trip.routemate.user.controller;

import com.trip.routemate.admin.security.AdminRolePolicy;
import com.trip.routemate.admin.service.AdminAuthorizationService;

import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.dto.UserLoginDto;
import com.trip.routemate.user.dto.UserLoginResponse;
import com.trip.routemate.user.dto.PasswordResetRequest;
import com.trip.routemate.user.repository.UserMstrRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "사용자 세션 로그인·현재 사용자·비밀번호 API")
public class AuthController {

    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호를 검증하고 JSESSIONID 세션을 생성합니다.")
    public ResponseEntity<UserLoginResponse> login(
            @Valid @RequestBody UserLoginDto dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UserMstr user = userMstrRepository.findByUserEmail(dto.getUserEmail().trim())
                .filter(this::isActiveUser)
                .filter(foundUser -> passwordEncoder.matches(dto.getUserPwd(), foundUser.getUserPwd()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "이메일 또는 비밀번호를 확인해 주세요."
                ));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                user.getUserEmail(),
                null,
                adminAuthorizationService.authoritiesFor(user.getUserId(), user.getUserRole())
        ));
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);

        return ResponseEntity.ok(toLoginResponse(user));
    }

    @GetMapping("/me")
    @Operation(summary = "현재 로그인 사용자 조회", description = "로그인 세션이 있으면 사용자와 관리자 권한 정보를 반환하고, 없으면 빈 응답을 반환합니다.")
    public ResponseEntity<UserLoginResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok().build();
        }
        UserMstr user = userMstrRepository.findByUserEmail(authentication.getName())
                .filter(this::isActiveUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        return ResponseEntity.ok(toLoginResponse(user));
    }

    /** 개발용 임시 재설정 API입니다. 이메일 인증 도입 후 반드시 교체해야 합니다. */
    @PostMapping("/password-reset")
    @Operation(summary = "비밀번호 재설정", description = "현재 개발용으로 제공되는 이메일 기반 비밀번호 변경 API입니다. 운영에서는 이메일 인증 절차로 교체해야 합니다.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        UserMstr user = userMstrRepository.findByUserEmail(request.userEmail().trim())
                .filter(this::isActiveUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "등록된 이메일을 찾을 수 없습니다."));
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userMstrRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    private UserLoginResponse toLoginResponse(UserMstr user) {
        return new UserLoginResponse(
                user.getUserId(),
                user.getUserEmail(),
                user.getUserNicknm(),
                user.getUserRole(),
                adminAuthorizationService.permissionNamesFor(user.getUserId(), user.getUserRole()),
                adminAuthorizationService.menuCodesFor(user.getUserId(), user.getUserRole())
        );
    }

    private boolean isActiveUser(UserMstr user) {
        return "ACTIVE".equals(user.getUserStatCd()) && "N".equals(user.getDelYn());
    }
}
