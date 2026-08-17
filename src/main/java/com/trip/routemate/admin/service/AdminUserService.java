package com.trip.routemate.admin.service;

import com.trip.routemate.admin.dto.AdminUserListResponse;
import com.trip.routemate.user.repository.UserMstrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "SUSPENDED");

    private final UserMstrRepository userMstrRepository;

    @PreAuthorize("hasAuthority('MEMBER_VIEW')")
    public AdminUserListResponse getUsers(String query, String status) {
        var normalizedQuery = query == null ? "" : query.trim();
        var normalizedStatus = normalizeFilterStatus(status);
        var summary = new AdminUserListResponse.Summary(
                userMstrRepository.countByUserRoleAndDelYn("USER", "N"),
                userMstrRepository.countByUserRoleAndUserStatCdAndDelYn("USER", "ACTIVE", "N"),
                userMstrRepository.countByUserRoleAndUserStatCdAndDelYn("USER", "SUSPENDED", "N")
        );
        var users = userMstrRepository.findAdminUsers(normalizedQuery, normalizedStatus)
                .stream()
                .map(AdminUserListResponse.UserItem::from)
                .toList();
        return new AdminUserListResponse(summary, users);
    }

    @Transactional
    @PreAuthorize("hasAuthority('MEMBER_STATUS_UPDATE')")
    public AdminUserListResponse.UserItem updateUserStatus(String adminEmail, Long userId, String status) {
        var normalizedStatus = normalizeUpdateStatus(status);
        var user = userMstrRepository.findByUserIdAndUserRoleAndDelYn(userId, "USER", "N")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        user.updateStatus(normalizedStatus);
        return AdminUserListResponse.UserItem.from(user);
    }

    private String normalizeFilterStatus(String status) {
        var normalized = status == null ? "ALL" : status.trim().toUpperCase();
        if ("ALL".equals(normalized) || ALLOWED_STATUSES.contains(normalized)) {
            return normalized;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 회원 상태입니다.");
    }

    private String normalizeUpdateStatus(String status) {
        var normalized = status == null ? "" : status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 회원 상태입니다.");
        }
        return normalized;
    }
}
