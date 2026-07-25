package com.trip.routemate.global.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest; // 👈 정확한 패키지 경로로 교체되었습니다!
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호 암호화 빈(Bean) 등록
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * 1. 정적 자원 우회 설정
     * 정적 파일들은 시큐리티 필터 자체를 타지 않도록 원천 차단하여 403 에러를 예방합니다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/assets/**",
                        "/favicon.ico",
                        "/index.html",
                        "/error" // 스프링 내부 에러 페이지 우회
                );
    }

    /**
     * 2. HTTP 보안 필터 체인 세부 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                // 비동기 Fetch 통신 및 Postman 요청을 위해 CSRF 보안 초기 해제
                .csrf(AbstractHttpConfigurer::disable)

                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                )

                // URL별 인가 관문 설정 (가장 직관적인 스프링 부트 3.x 표준 문자열 매칭)
                .authorizeHttpRequests(auth -> auth
                        // 공개 화면에서 사용하는 API도 함께 허용해야 브라우저의 비동기 요청이 차단되지 않습니다.
                        .requestMatchers("/", "/join", "/api/user/join", "/api/auth/login", "/api/home/data", "/login", "/lotto", "/api/lotto/numbers").permitAll()
                        // 그 외 모든 요청은 인증 잠금
                        .anyRequest().authenticated()
                )

                // 기본 로그인창 가로채기 및 HTTP Basic 인증 해제
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .permitAll()
                );

        return http.build();
    }
}
