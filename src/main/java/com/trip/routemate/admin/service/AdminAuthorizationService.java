package com.trip.routemate.admin.service;

import com.trip.routemate.admin.repository.AdminRolePermissionRepository;
import com.trip.routemate.admin.repository.AdminUserRoleRepository;
import com.trip.routemate.admin.repository.AdminRoleMenuRepository;
import com.trip.routemate.admin.repository.AdminRoleRepository;
import com.trip.routemate.admin.security.AdminRolePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuthorizationService {

    private final AdminUserRoleRepository adminUserRoleRepository;
    private final AdminRolePermissionRepository adminRolePermissionRepository;
    private final AdminRoleMenuRepository adminRoleMenuRepository;
    private final AdminRoleRepository adminRoleRepository;

    public boolean isStaffRole(String roleCode) {
        return adminRoleRepository.findByRoleCodeAndUseYn(roleCode, "Y")
                .map(role -> true)
                .orElseGet(() -> AdminRolePolicy.isStaffRole(roleCode));
    }

    public List<String> permissionNamesFor(Long userId, String legacyRole) {
        var roleCodes = adminUserRoleRepository.findPrimaryRoleCodesByUserId(userId);
        if (roleCodes.isEmpty()) {
            return AdminRolePolicy.permissionNamesFor(legacyRole);
        }
        return adminRolePermissionRepository.findPermissionCodesByRoleCodes(roleCodes)
                .stream()
                .distinct()
                .sorted()
                .toList();
    }

    public List<GrantedAuthority> authoritiesFor(Long userId, String legacyRole) {
        var authorities = new ArrayList<GrantedAuthority>();
        permissionNamesFor(userId, legacyRole).stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        authorities.add(new SimpleGrantedAuthority("ROLE_" + legacyRole));
        return List.copyOf(authorities);
    }

    public List<String> menuCodesFor(Long userId, String legacyRole) {
        var roleCodes = adminUserRoleRepository.findPrimaryRoleCodesByUserId(userId);
        if (roleCodes.isEmpty()) {
            return permissionNamesFor(userId, legacyRole).stream()
                    .map(permission -> switch (permission) {
                        case "DASHBOARD_VIEW" -> "DASHBOARD";
                        case "MEMBER_VIEW", "MEMBER_STATUS_UPDATE" -> "MEMBERS";
                        case "STAFF_VIEW", "STAFF_MANAGE" -> "STAFF";
                        case "PLAN_MANAGE" -> "PLANS";
                        case "DESTINATION_MANAGE" -> "DESTINATIONS";
                        default -> null;
                    })
                    .filter(java.util.Objects::nonNull)
                    .flatMap(menu -> "DESTINATIONS".equals(menu) ? java.util.stream.Stream.of("DESTINATIONS", "RECOMMENDATIONS") : java.util.stream.Stream.of(menu))
                    .distinct()
                    .toList();
        }
        return adminRoleMenuRepository.findMenuCodesByRoleCodes(roleCodes)
                .stream()
                .distinct()
                .toList();
    }
}
