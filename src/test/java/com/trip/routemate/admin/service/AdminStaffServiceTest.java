package com.trip.routemate.admin.service;

import com.trip.routemate.admin.security.AdminRolePolicy;
import com.trip.routemate.admin.dto.AdminStaffCreateRequest;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AdminStaffServiceTest {

    @Mock private UserMstrRepository userMstrRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AdminStaffService adminStaffService;

    @Test
    void getStaff_returnsRoleSummaryAndStaffOnly() {
        var roles = AdminRolePolicy.staffRoles();
        when(userMstrRepository.countByUserRoleInAndDelYn(roles, "N")).thenReturn(4L);
        when(userMstrRepository.countByUserRoleInAndUserStatCdAndDelYn(roles, "ACTIVE", "N")).thenReturn(3L);
        when(userMstrRepository.countByUserRoleInAndUserStatCdAndDelYn(roles, "SUSPENDED", "N")).thenReturn(1L);
        when(userMstrRepository.countByUserRoleAndDelYn("ADMIN", "N")).thenReturn(1L);
        when(userMstrRepository.countByUserRoleAndDelYn("MASTER", "N")).thenReturn(1L);
        when(userMstrRepository.countByUserRoleAndDelYn("SENIOR", "N")).thenReturn(1L);
        when(userMstrRepository.countByUserRoleAndDelYn("JUNIOR", "N")).thenReturn(1L);
        when(userMstrRepository.findAdminStaff("운영", "ACTIVE", "ALL"))
                .thenReturn(List.of(staff(2L, "MASTER")));

        var result = adminStaffService.getStaff(" 운영 ", "active", "all");

        assertThat(result.summary().totalStaff()).isEqualTo(4L);
        assertThat(result.summary().masterCount()).isEqualTo(1L);
        assertThat(result.staff()).extracting("userRole").containsExactly("MASTER");
    }

    @Test
    void updateRole_changesManageableStaffRole() {
        var junior = staff(2L, "JUNIOR");
        when(userMstrRepository.findByUserIdAndUserRoleInAndDelYn(2L, AdminRolePolicy.staffRoles(), "N"))
                .thenReturn(Optional.of(junior));

        var result = adminStaffService.updateRole("admin@routemate.com", 2L, "SENIOR");

        assertThat(result.userRole()).isEqualTo("SENIOR");
        assertThat(junior.getUserRole()).isEqualTo("SENIOR");
    }

    @Test
    void createStaff_createsActiveLocalStaffAccount() {
        var request = new AdminStaffCreateRequest(" Staff@RouteMate.com ", "password123", "신입 운영자", "JUNIOR");
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userMstrRepository.save(any(UserMstr.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = adminStaffService.createStaff(request);

        assertThat(result.userEmail()).isEqualTo("staff@routemate.com");
        assertThat(result.userRole()).isEqualTo("JUNIOR");
        assertThat(result.userStatCd()).isEqualTo("ACTIVE");
    }

    @Test
    void updateStatus_protectsAdminAccount() {
        when(userMstrRepository.findByUserIdAndUserRoleInAndDelYn(1L, AdminRolePolicy.staffRoles(), "N"))
                .thenReturn(Optional.of(staff(1L, "ADMIN")));

        assertThatThrownBy(() -> adminStaffService.updateStatus("other@routemate.com", 1L, "SUSPENDED"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private UserMstr staff(Long id, String role) {
        return UserMstr.builder()
                .userId(id)
                .userEmail(id == 1L ? "admin@routemate.com" : "staff@routemate.com")
                .userNicknm("운영자")
                .userRole(role)
                .userStatCd("ACTIVE")
                .delYn("N")
                .build();
    }
}
