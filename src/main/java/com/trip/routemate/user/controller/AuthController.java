package com.trip.routemate.user.controller;

import com.trip.routemate.user.domain.UserMstr;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
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
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole()))
        ));
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);

        return ResponseEntity.ok(toLoginResponse(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserLoginResponse> getCurrentUser(Authentication authentication) {
        UserMstr user = userMstrRepository.findByUserEmail(authentication.getName())
                .filter(this::isActiveUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        return ResponseEntity.ok(toLoginResponse(user));
    }

    private UserLoginResponse toLoginResponse(UserMstr user) {
        return new UserLoginResponse(user.getUserId(), user.getUserEmail(), user.getUserNicknm());
    }

    private boolean isActiveUser(UserMstr user) {
        return "ACTIVE".equals(user.getUserStatCd()) && "N".equals(user.getDelYn());
    }
}
