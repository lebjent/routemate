package com.trip.routemate.admin.service;

import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private UserMstrRepository userMstrRepository;

    @InjectMocks private AdminUserService adminUserService;

    @Test
    void getUsers_returnsSummaryAndFilteredMembers() {
        when(userMstrRepository.countByUserRoleAndDelYn("USER", "N")).thenReturn(5L);
        when(userMstrRepository.countByUserRoleAndUserStatCdAndDelYn("USER", "ACTIVE", "N")).thenReturn(3L);
        when(userMstrRepository.countByUserRoleAndUserStatCdAndDelYn("USER", "SUSPENDED", "N")).thenReturn(1L);
        when(userMstrRepository.findAdminUsers("여행", "ACTIVE")).thenReturn(List.of(user(2L, "USER")));

        var result = adminUserService.getUsers(" 여행 ", "active");

        assertThat(result.summary().totalUsers()).isEqualTo(5L);
        assertThat(result.summary().suspendedUsers()).isEqualTo(1L);
        assertThat(result.users()).hasSize(1);
    }

    @Test
    void updateUserStatus_suspendsRegularMember() {
        var member = user(2L, "USER");
        when(userMstrRepository.findByUserIdAndUserRoleAndDelYn(2L, "USER", "N")).thenReturn(Optional.of(member));

        var result = adminUserService.updateUserStatus("admin@routemate.com", 2L, "SUSPENDED");

        assertThat(result.userStatCd()).isEqualTo("SUSPENDED");
        assertThat(member.getUserStatCd()).isEqualTo("SUSPENDED");
    }

    @Test
    void updateUserStatus_doesNotExposeStaffAccount() {
        when(userMstrRepository.findByUserIdAndUserRoleAndDelYn(1L, "USER", "N"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateUserStatus("other-admin@routemate.com", 1L, "SUSPENDED"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private UserMstr user(Long id, String role) {
        return UserMstr.builder()
                .userId(id)
                .userEmail(id == 1L ? "admin@routemate.com" : "user@routemate.com")
                .userNicknm("여행자")
                .userRole(role)
                .userStatCd("ACTIVE")
                .delYn("N")
                .build();
    }
}
