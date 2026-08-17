package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminStaffListResponse;
import com.trip.routemate.admin.dto.AdminStaffCreateRequest;
import com.trip.routemate.admin.security.AdminRolePolicy;
import com.trip.routemate.user.domain.UserMstr;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStaffService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "SUSPENDED");
    private static final Set<String> MANAGEABLE_ROLES = Set.of(
            AdminRolePolicy.MASTER,
            AdminRolePolicy.SENIOR,
            AdminRolePolicy.JUNIOR
    );

    private final UserMstrRepository userMstrRepository;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAuthority('STAFF_VIEW')")
    public AdminStaffListResponse getStaff(String query, String status, String role) {
        var normalizedQuery = query == null ? "" : query.trim();
        var normalizedStatus = normalizeStatusFilter(status);
        var normalizedRole = normalizeRoleFilter(role);
        var staffRoles = AdminRolePolicy.staffRoles();
        var summary = new AdminStaffListResponse.Summary(
                userMstrRepository.countByUserRoleInAndDelYn(staffRoles, "N"),
                userMstrRepository.countByUserRoleInAndUserStatCdAndDelYn(staffRoles, "ACTIVE", "N"),
                userMstrRepository.countByUserRoleInAndUserStatCdAndDelYn(staffRoles, "SUSPENDED", "N"),
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
        var staff = UserMstr.builder()
                .userEmail(email)
                .userPwd(passwordEncoder.encode(request.userPwd()))
                .userNicknm(nickname)
                .snsProvider("LOCAL")
                .userRole(role)
                .userStatCd("ACTIVE")
                .delYn("N")
                .build();
        return AdminStaffListResponse.StaffItem.from(userMstrRepository.save(staff));
    }

    @Transactional
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    public AdminStaffListResponse.StaffItem updateRole(String actorEmail, Long userId, String role) {
        var normalizedRole = normalizeManageableRole(role);
        var staff = getManageableStaff(actorEmail, userId);
        staff.updateRole(normalizedRole);
        return AdminStaffListResponse.StaffItem.from(staff);
    }

    @Transactional
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    public AdminStaffListResponse.StaffItem updateStatus(String actorEmail, Long userId, String status) {
        var normalizedStatus = normalizeStatusUpdate(status);
        var staff = getManageableStaff(actorEmail, userId);
        staff.updateStatus(normalizedStatus);
        return AdminStaffListResponse.StaffItem.from(staff);
    }

    private UserMstr getManageableStaff(String actorEmail, Long userId) {
        var staff = userMstrRepository.findByUserIdAndUserRoleInAndDelYn(
                        userId,
                        AdminRolePolicy.staffRoles(),
                        "N"
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "직원 계정을 찾을 수 없습니다."));
        if (staff.getUserEmail().equalsIgnoreCase(actorEmail) || AdminRolePolicy.ADMIN.equals(staff.getUserRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인 또는 ADMIN 계정은 변경할 수 없습니다.");
        }
        return staff;
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
        if ("ALL".equals(normalized) || AdminRolePolicy.isStaffRole(normalized)) return normalized;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 직원 권한입니다.");
    }

    private String normalizeManageableRole(String role) {
        var normalized = role == null ? "" : role.trim().toUpperCase();
        if (MANAGEABLE_ROLES.contains(normalized)) return normalized;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MASTER, SENIOR, JUNIOR 권한만 지정할 수 있습니다.");
    }
}
