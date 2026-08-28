package com.trip.routemate.user.controller;

import com.trip.routemate.admin.security.AdminRolePolicy;
import com.trip.routemate.common.security.AuthorizationService;

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

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin Authentication", description = "관리자 세션 로그인 API")
public class AdminAuthController {

    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final AuthorizationService authorizationService;

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

        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                admin.getUserEmail(),
                null,
                authorizationService.authoritiesFor(admin.getUserId(), admin.getUserRole())
        ));
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);

        return ResponseEntity.ok(new UserLoginResponse(
                admin.getUserId(),
                admin.getUserEmail(),
                admin.getUserNicknm(),
                admin.getUserRole(),
                authorizationService.permissionNamesFor(admin.getUserId(), admin.getUserRole()),
                authorizationService.menuCodesFor(admin.getUserId(), admin.getUserRole())
        ));
    }

    private boolean isActiveStaff(UserMstr user) {
        return authorizationService.isStaffRole(user.getUserRole())
                && "ACTIVE".equals(user.getUserStatCd())
                && "N".equals(user.getDelYn());
    }
}
