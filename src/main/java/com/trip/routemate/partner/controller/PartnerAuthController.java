package com.trip.routemate.partner.controller;

import com.trip.routemate.admin.service.AdminAuthorizationService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner/auth")
public class PartnerAuthController {
    private final UserMstrRepository userMstrRepository;
    private final PartnerUserRepository partnerUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginDto request,
                                                   HttpServletRequest servletRequest, HttpServletResponse response) {
        var user = userMstrRepository.findByUserEmail(request.getUserEmail().trim())
                .filter(found -> passwordEncoder.matches(request.getUserPwd(), found.getUserPwd()))
                .filter(found -> "ACTIVE".equals(found.getUserStatCd()) && "N".equals(found.getDelYn()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "파트너 직원 ID 또는 비밀번호를 확인해 주세요."));
        var partnerUser = partnerUserRepository.findByUserUserEmailAndUseYn(user.getUserEmail(), "Y")
                .filter(mapping -> "ACTIVE".equals(mapping.getPartner().getPartnerStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "활성 상태의 파트너사 직원 계정이 아닙니다."));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user.getUserEmail(), null,
                adminAuthorizationService.authoritiesFor(user.getUserId(), user.getUserRole())));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, response);
        return ResponseEntity.ok(new UserLoginResponse(user.getUserId(), user.getUserEmail(), user.getUserNicknm(), user.getUserRole(),
                adminAuthorizationService.permissionNamesFor(user.getUserId(), user.getUserRole()),
                adminAuthorizationService.menuCodesFor(user.getUserId(), user.getUserRole())));
    }
}
