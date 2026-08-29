package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminStaffListResponse;
import com.trip.routemate.admin.dto.AdminStaffCreateRequest;
import com.trip.routemate.admin.domain.AdminUserRole;
import com.trip.routemate.admin.repository.AdminRoleRepository;
import com.trip.routemate.admin.repository.AdminUserRoleRepository;
import com.trip.routemate.admin.security.AdminRolePolicy;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/** 관리자 직원의 생성, 역할 변경, 사용 상태 변경을 처리한다. */
public class AdminStaffService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "SUSPENDED");
    private static final Set<String> MANAGEABLE_ROLES = Set.of(
            AdminRolePolicy.MASTER,
            AdminRolePolicy.SENIOR,
            AdminRolePolicy.JUNIOR
    );

    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRoleRepository adminRoleRepository;
    private final AdminUserRoleRepository adminUserRoleRepository;

    @PreAuthorize("hasAuthority('STAFF_VIEW')")
    /** 검색어, 계정 상태, 역할 조건으로 관리자 직원 목록을 조회한다. */
    public AdminStaffListResponse getStaff(String query, String status, String role) {
        var normalizedQuery = query == null ? "" : query.trim();
        var normalizedStatus = normalizeStatusFilter(status);
        var normalizedRole = normalizeRoleFilter(role);
        var summary = new AdminStaffListResponse.Summary(
                userMstrRepository.countStaffByDelYn("N"),
                userMstrRepository.countStaffByStatusAndDelYn("ACTIVE", "N"),
                userMstrRepository.countStaffByStatusAndDelYn("SUSPENDED", "N"),
                userMstrRepository.countByUserRoleAndDelYn(AdminRolePolicy.ADMIN, "N"),
                userMstrRepository.countByUserRoleAndDelYn(AdminRolePolicy.MASTER, "N"),
                userMstrRepository.countByUserRoleAndDelYn(AdminRolePolicy.SENIOR, "N"),
                userMstrRepository.countByUserRoleAndDelYn(AdminRolePolicy.JUNIOR, "N")
        );
        var staff = userMstrRepository.findAdminStaff(normalizedQuery, normalizedStatus, normalizedRole)
                .stream()
                .map(AdminStaffListResponse.StaffItem::from)
                .toList();
        return new AdminStaffListResponse(summary, staff);
    }

    @Transactional
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    /** 이메일 중복을 검증하고 관리자 역할을 가진 직원 계정을 생성한다. */
    public AdminStaffListResponse.StaffItem createStaff(AdminStaffCreateRequest request) {
        var email = request.userEmail().trim().toLowerCase();
        var nickname = request.userNicknm().trim();
        var role = normalizeManageableRole(request.userRole());
        if (userMstrRepository.existsByUserEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        if (userMstrRepository.existsByUserNicknm(nickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
        @NonNull UserMstr staff = Objects.requireNonNull(UserMstr.builder()
                .userEmail(email)
                .userPwd(passwordEncoder.encode(request.userPwd()))
                .userNicknm(nickname)
                .snsProvider("LOCAL")
                .userRole(role)
                .userStatCd("ACTIVE")
                .delYn("N")
                .build(), "직원 계정을 생성할 수 없습니다.");
        var saved = userMstrRepository.save(staff);
        adminRoleRepository.findByRoleCodeAndUseYn(role, "Y").ifPresent(adminRole ->
                adminUserRoleRepository.save(Objects.requireNonNull(AdminUserRole.builder()
                        .userId(saved.getUserId())
                        .roleId(adminRole.getRoleId())
                        .primaryYn("Y")
                        .build()))
        );
        return AdminStaffListResponse.StaffItem.from(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    /** 현재 로그인한 관리자 자신을 제외하고 직원 역할을 변경한다. */
    public AdminStaffListResponse.StaffItem updateRole(String actorEmail, Long userId, String role) {
        var normalizedRole = normalizeManageableRole(role);
        var staff = getManageableStaff(actorEmail, userId);
        staff.updateRole(normalizedRole);
        syncPrimaryRole(staff.getUserId(), normalizedRole);
        return AdminStaffListResponse.StaffItem.from(staff);
    }

    @Transactional
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    /** 현재 로그인한 관리자 자신을 제외하고 직원 계정 상태를 변경한다. */
    public AdminStaffListResponse.StaffItem updateStatus(String actorEmail, Long userId, String status) {
        var normalizedStatus = normalizeStatusUpdate(status);
        var staff = getManageableStaff(actorEmail, userId);
        staff.updateStatus(normalizedStatus);
        return AdminStaffListResponse.StaffItem.from(staff);
    }

    private UserMstr getManageableStaff(String actorEmail, Long userId) {
        var staff = userMstrRepository.findStaffByUserIdAndDelYn(userId, "N")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "직원 계정을 찾을 수 없습니다."));
        if (staff.getUserEmail().equalsIgnoreCase(actorEmail) || AdminRolePolicy.ADMIN.equals(staff.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인 또는 ADMIN 계정은 변경할 수 없습니다.");
        }
        return staff;
    }

    private void syncPrimaryRole(Long userId, String roleCode) {
        adminRoleRepository.findByRoleCodeAndUseYn(roleCode, "Y").ifPresent(adminRole -> {
            adminUserRoleRepository.deleteAllByUserId(userId);
            adminUserRoleRepository.save(Objects.requireNonNull(AdminUserRole.builder()
                    .userId(userId)
                    .roleId(adminRole.getRoleId())
                    .primaryYn("Y")
                    .build()));
        });
    }

    private String normalizeStatusFilter(String status) {
        var normalized = status == null ? "ALL" : status.trim().toUpperCase();
        if ("ALL".equals(normalized) || ALLOWED_STATUSES.contains(normalized)) return normalized;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 직원 상태입니다.");
    }

    private String normalizeStatusUpdate(String status) {
        var normalized = status == null ? "" : status.trim().toUpperCase();
        if (ALLOWED_STATUSES.contains(normalized)) return normalized;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 직원 상태입니다.");
    }

    private String normalizeRoleFilter(String role) {
        var normalized = role == null ? "ALL" : role.trim().toUpperCase();
        if ("ALL".equals(normalized) || adminRoleRepository.findByRoleCodeAndUseYn(normalized, "Y").isPresent() || AdminRolePolicy.isStaffRole(normalized)) return normalized;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 직원 권한입니다.");
    }

    private String normalizeManageableRole(String role) {
        var normalized = role == null ? "" : role.trim().toUpperCase();
        if (MANAGEABLE_ROLES.contains(normalized) || adminRoleRepository.findByRoleCodeAndUseYn(normalized, "Y").map(adminRole -> adminRole.getRoleLevel() < 100).orElse(false)) return normalized;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MASTER, SENIOR, JUNIOR 권한만 지정할 수 있습니다.");
    }
}
