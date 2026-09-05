package com.trip.routemate.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

/** 로그인 종류와 무관하게 세션 보안 컨텍스트를 저장하는 공통 서비스. */
@Service
@RequiredArgsConstructor
public class SessionAuthenticationService {
    private final SecurityContextRepository repository;

    public void login(String username, HttpServletRequest request, HttpServletResponse response,
                      Collection<? extends GrantedAuthority> authorities) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(username, null, authorities));
        SecurityContextHolder.setContext(context);
        repository.saveContext(context, request, response);
    }
}
