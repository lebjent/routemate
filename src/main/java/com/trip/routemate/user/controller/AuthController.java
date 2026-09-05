package com.trip.routemate.user.controller;

import com.trip.routemate.common.security.AuthorizationService;
import com.trip.routemate.common.security.SessionAuthenticationService;

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
import org.springframework.beans.factory.annotation.Value;

/**
 * 일반 사용자의 세션 로그인과 현재 사용자 조회를 제공하는 API다.
 *
 * 인증이 성공하면 권한 정보가 포함된 Spring Security 컨텍스트를 서버 세션에 저장한다.
 * 비밀번호 재설정은 현재 개발용 임시 기능이므로 운영 환경에서는 본인 인증 절차로 대체해야 한다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "사용자 세션 로그인·현재 사용자·비밀번호 API")
public class AuthController {

    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final AuthorizationService authorizationService;
    private final SessionAuthenticationService sessionAuthenticationService;

    @Value("${app.auth.password-reset-enabled:false}")
    private boolean passwordResetEnabled;

    /**
     * 이메일과 비밀번호를 검증하고 로그인 세션을 생성한다.
     *
     * 탈퇴되었거나 비활성화된 계정은 로그인할 수 없다.
     *
     * @param dto 로그인 이메일과 비밀번호
     * @param request 현재 HTTP 요청. 세션 보안 컨텍스트 저장에 사용한다.
     * @param response 현재 HTTP 응답. 세션 식별자 전달에 사용한다.
     * @return 로그인 사용자와 적용 권한·메뉴 정보
     */
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

        if (sessionAuthenticationService != null) {
            sessionAuthenticationService.login(user.getUserEmail(), request, response,
                    authorizationService.authoritiesFor(user.getUserId(), user.getUserRole()));
        } else {
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user.getUserEmail(), null,
                    authorizationService.authoritiesFor(user.getUserId(), user.getUserRole())));
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
        }

        return ResponseEntity.ok(toLoginResponse(user));
    }

    /**
     * 현재 세션의 로그인 사용자를 조회한다.
     *
     * 로그인 세션이 없을 때 오류 대신 빈 200 응답을 반환해 클라이언트가 초기 인증 상태를
     * 자연스럽게 판단할 수 있게 한다.
     *
     * @param authentication 현재 요청의 인증 정보
     * @return 로그인 사용자 정보 또는 빈 응답
     */
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

    /**
     * 개발 환경에서만 사용하는 임시 비밀번호 재설정 기능이다.
     *
     * 이메일 소유 여부를 확인하지 않으므로 운영 배포 전 이메일 또는 휴대전화 인증 기반의
     * 비밀번호 재설정 절차로 반드시 교체해야 한다.
     *
     * @param request 대상 이메일과 새 비밀번호
     * @return 성공 시 HTTP 204
     */
    @PostMapping("/password-reset")
    @Operation(summary = "비밀번호 재설정", description = "현재 개발용으로 제공되는 이메일 기반 비밀번호 변경 API입니다. 운영에서는 이메일 인증 절차로 교체해야 합니다.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        if (!passwordResetEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "비밀번호 재설정 기능이 활성화되지 않았습니다.");
        }
        UserMstr user = userMstrRepository.findByUserEmail(request.userEmail().trim())
                .filter(this::isActiveUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "등록된 이메일을 찾을 수 없습니다."));
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        userMstrRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    /** 인증된 회원 엔티티를 프런트엔드가 사용하는 로그인 응답으로 변환한다. */
    private UserLoginResponse toLoginResponse(UserMstr user) {
        return new UserLoginResponse(
                user.getUserId(),
                user.getUserEmail(),
                user.getUserNicknm(),
                user.getUserRole(),
                authorizationService.permissionNamesFor(user.getUserId(), user.getUserRole()),
                authorizationService.menuCodesFor(user.getUserId(), user.getUserRole())
        );
    }

    /** 계정이 활성 상태이고 탈퇴 처리되지 않았는지 확인한다. */
    private boolean isActiveUser(UserMstr user) {
        return "ACTIVE".equals(user.getUserStatCd()) && "N".equals(user.getDelYn());
    }
}
