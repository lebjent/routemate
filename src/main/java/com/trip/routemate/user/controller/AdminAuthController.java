package com.trip.routemate.user.controller;

import com.trip.routemate.common.security.AuthorizationService;
import com.trip.routemate.common.security.SessionAuthenticationService;

import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.dto.UserLoginDto;
import com.trip.routemate.user.dto.UserLoginResponse;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 관리자 직원 전용 세션 로그인을 제공하는 API다.
 *
 * 활성 상태의 관리자 역할 계정만 인증할 수 있으며, 일반 회원과 파트너 직원 계정은 이 경로로
 * 관리자 세션을 만들 수 없다.
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin Authentication", description = "관리자 세션 로그인 API")
public class AdminAuthController {

    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final AuthorizationService authorizationService;
    private final SessionAuthenticationService sessionAuthenticationService;

    /**
     * 관리자 역할과 계정 상태를 검증한 뒤 관리자 세션을 생성한다.
     *
     * @param dto 관리자 이메일과 비밀번호
     * @param request 현재 HTTP 요청. 세션 보안 컨텍스트 저장에 사용한다.
     * @param response 현재 HTTP 응답. 세션 식별자 전달에 사용한다.
     * @return 로그인한 관리자와 권한·메뉴 정보
     */
    @PostMapping("/login")
    @Operation(summary = "관리자 로그인", description = "관리자 계정을 검증하고 관리자 권한이 포함된 JSESSIONID 세션을 생성합니다.")
    public ResponseEntity<UserLoginResponse> login(
            @Valid @RequestBody UserLoginDto dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UserMstr admin = userMstrRepository.findByUserEmail(dto.getUserEmail().trim())
                .filter(this::isActiveStaff)
                .filter(foundUser -> passwordEncoder.matches(dto.getUserPwd(), foundUser.getUserPwd()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "관리자 계정 정보를 확인해 주세요."
                ));

        if (sessionAuthenticationService != null) {
            sessionAuthenticationService.login(admin.getUserEmail(), request, response,
                    authorizationService.authoritiesFor(admin.getUserId(), admin.getUserRole()));
        } else {
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(admin.getUserEmail(), null,
                    authorizationService.authoritiesFor(admin.getUserId(), admin.getUserRole())));
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
        }

        return ResponseEntity.ok(new UserLoginResponse(
                admin.getUserId(),
                admin.getUserEmail(),
                admin.getUserNicknm(),
                admin.getUserRole(),
                authorizationService.permissionNamesFor(admin.getUserId(), admin.getUserRole()),
                authorizationService.menuCodesFor(admin.getUserId(), admin.getUserRole())
        ));
    }

    /** 관리자 역할, 활성 상태, 미탈퇴 상태를 모두 만족하는지 확인한다. */
    private boolean isActiveStaff(UserMstr user) {
        return authorizationService.isStaffRole(user.getUserRole())
                && "ACTIVE".equals(user.getUserStatCd())
                && "N".equals(user.getDelYn());
    }
}
