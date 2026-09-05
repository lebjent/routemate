package com.trip.routemate.partner.controller;

import com.trip.routemate.common.security.AuthorizationService;
import com.trip.routemate.common.security.SessionAuthenticationService;
import com.trip.routemate.partner.repository.PartnerUserRepository;
import com.trip.routemate.user.dto.UserLoginDto;
import com.trip.routemate.user.dto.UserLoginResponse;
import com.trip.routemate.user.repository.UserMstrRepository;
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
 * 파트너사 대표와 직원의 포털 인증을 처리하는 API다.
 *
 * 일반 회원 로그인과 달리 활성 파트너사에 사용 중인 직원으로 연결된 계정만 인증한다.
 * 로그인에 성공하면 파트너 포털에서 필요한 권한과 메뉴 정보를 세션에 함께 저장한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner/auth")
public class PartnerAuthController {
    private final UserMstrRepository userMstrRepository;
    private final PartnerUserRepository partnerUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final AuthorizationService authorizationService;
    private final SessionAuthenticationService sessionAuthenticationService;

    /**
     * 파트너 포털에 로그인하고 서버 세션을 만든다.
     *
     * 계정 비밀번호, 회원 상태, 파트너 직원 소속 및 파트너사 상태를 모두 확인한다.
     * 어느 하나라도 충족하지 않으면 인증 실패 또는 접근 금지 응답을 반환한다.
     *
     * @param request 이메일과 비밀번호
     * @param servletRequest 현재 HTTP 요청. 세션 보안 컨텍스트 저장에 사용한다.
     * @param response 현재 HTTP 응답. 세션 식별자 전달에 사용한다.
     * @return 로그인 사용자와 권한·메뉴 정보
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginDto request,
                                                   HttpServletRequest servletRequest, HttpServletResponse response) {
        var user = userMstrRepository.findByUserEmail(request.getUserEmail().trim())
                .filter(found -> passwordEncoder.matches(request.getUserPwd(), found.getUserPwd()))
                .filter(found -> "ACTIVE".equals(found.getUserStatCd()) && "N".equals(found.getDelYn()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "파트너 직원 ID 또는 비밀번호를 확인해 주세요."));
        partnerUserRepository.findByUserUserEmailAndUseYn(user.getUserEmail(), "Y")
                .filter(mapping -> "ACTIVE".equals(mapping.getPartner().getPartnerStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "활성 상태의 파트너사 직원 계정이 아닙니다."));
        if (sessionAuthenticationService != null) {
            sessionAuthenticationService.login(user.getUserEmail(), servletRequest, response,
                    authorizationService.authoritiesFor(user.getUserId(), user.getUserRole()));
        } else {
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user.getUserEmail(), null,
                    authorizationService.authoritiesFor(user.getUserId(), user.getUserRole())));
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, servletRequest, response);
        }
        return ResponseEntity.ok(new UserLoginResponse(user.getUserId(), user.getUserEmail(), user.getUserNicknm(), user.getUserRole(),
                authorizationService.permissionNamesFor(user.getUserId(), user.getUserRole()),
                authorizationService.menuCodesFor(user.getUserId(), user.getUserRole())));
    }
}
