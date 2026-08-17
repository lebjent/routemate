package com.trip.routemate.user.controller;

import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.dto.UserLoginDto;
import com.trip.routemate.user.repository.UserMstrRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    @Mock private UserMstrRepository userMstrRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SecurityContextRepository securityContextRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks private AdminAuthController adminAuthController;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_createsAdminSessionForActiveAdmin() {
        var dto = loginDto();
        var admin = user("ADMIN");
        when(userMstrRepository.findByUserEmail("admin@routemate.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password", admin.getUserPwd())).thenReturn(true);

        var result = adminAuthController.login(dto, request, response);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().userRole()).isEqualTo("ADMIN");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN", "DASHBOARD_VIEW", "MEMBER_VIEW", "STAFF_MANAGE");
        assertThat(result.getBody().permissions()).contains("DASHBOARD_VIEW", "STAFF_MANAGE");
        verify(securityContextRepository).saveContext(any(SecurityContext.class), any(), any());
    }

    @Test
    void login_rejectsRegularUserEvenWhenPasswordMatches() {
        var dto = loginDto();
        when(userMstrRepository.findByUserEmail("admin@routemate.com"))
                .thenReturn(Optional.of(user("USER")));

        assertThatThrownBy(() -> adminAuthController.login(dto, request, response))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(securityContextRepository, never()).saveContext(any(), any(), any());
    }

    @Test
    void login_allowsActiveSeniorWithAssignedPermissions() {
        var dto = loginDto();
        var senior = user("SENIOR");
        when(userMstrRepository.findByUserEmail("admin@routemate.com")).thenReturn(Optional.of(senior));
        when(passwordEncoder.matches("password", senior.getUserPwd())).thenReturn(true);

        var result = adminAuthController.login(dto, request, response);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().permissions())
                .containsExactlyInAnyOrder("DASHBOARD_VIEW", "MEMBER_VIEW", "PLAN_MANAGE");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("ROLE_SENIOR", "MEMBER_VIEW")
                .doesNotContain("MEMBER_STATUS_UPDATE", "STAFF_VIEW");
    }

    private UserLoginDto loginDto() {
        var dto = new UserLoginDto();
        dto.setUserEmail("admin@routemate.com");
        dto.setUserPwd("password");
        return dto;
    }

    private UserMstr user(String role) {
        return UserMstr.builder()
                .userId(1L)
                .userEmail("admin@routemate.com")
                .userPwd("encoded-password")
                .userNicknm("운영자")
                .userRole(role)
                .userStatCd("ACTIVE")
                .delYn("N")
                .build();
    }
}
