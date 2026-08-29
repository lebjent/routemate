package com.trip.routemate.admin.service;

import com.trip.routemate.admin.repository.AdminRolePermissionRepository;
import com.trip.routemate.admin.repository.AdminUserRoleRepository;
import com.trip.routemate.admin.repository.AdminRoleMenuRepository;
import com.trip.routemate.admin.repository.AdminRoleRepository;
import com.trip.routemate.admin.security.AdminRolePolicy;
import com.trip.routemate.common.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
/** 관리자 역할, 권한, 메뉴 접근 정보를 현재 사용자에게 부여한다. */
public class AdminAuthorizationService implements AuthorizationService {

    private final AdminUserRoleRepository adminUserRoleRepository;
    private final AdminRolePermissionRepository adminRolePermissionRepository;
    private final AdminRoleMenuRepository adminRoleMenuRepository;
    private final AdminRoleRepository adminRoleRepository;

    /** 전달된 역할 코드가 관리자 직원 역할인지 확인한다. */
    public boolean isStaffRole(String roleCode) {
        return adminRoleRepository.findByRoleCodeAndUseYn(roleCode, "Y")
                .map(role -> true)
                .orElseGet(() -> AdminRolePolicy.isStaffRole(roleCode));
    }

    /** 사용자에게 적용되는 권한명을 조회한다. 역할 매핑이 없으면 기존 역할 규칙을 사용한다. */
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

    /** Spring Security 인증 객체에 넣을 권한 목록을 만든다. */
    public List<GrantedAuthority> authoritiesFor(Long userId, String legacyRole) {
        var authorities = new ArrayList<GrantedAuthority>();
        permissionNamesFor(userId, legacyRole).stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        authorities.add(new SimpleGrantedAuthority("ROLE_" + legacyRole));
        return List.copyOf(authorities);
    }

    /** 로그인한 사용자가 접근할 수 있는 관리자 메뉴 코드를 조회한다. */
    public List<String> menuCodesFor(Long userId, String legacyRole) {
        var roleCodes = adminUserRoleRepository.findPrimaryRoleCodesByUserId(userId);
        if (roleCodes.isEmpty()) {
            return permissionNamesFor(userId, legacyRole).stream()
                    .map(permission -> switch (permission) {
                        case "DASHBOARD_VIEW" -> "DASHBOARD";
                        case "MEMBER_VIEW", "MEMBER_STATUS_UPDATE" -> "MEMBERS";
                        case "STAFF_VIEW", "STAFF_MANAGE" -> "STAFF";
                        case "PLAN_MANAGE" -> "PLANS";
                        case "PARTNER_MANAGE" -> "PARTNERS";
                        case "DESTINATION_MANAGE" -> "DESTINATIONS";
                        default -> null;
                    })
                    .filter(java.util.Objects::nonNull)
                    .flatMap(menu -> switch (menu) {
                        case "DESTINATIONS" -> java.util.stream.Stream.of("DESTINATIONS", "RECOMMENDATIONS", "PRODUCTS");
                        case "PARTNERS" -> java.util.stream.Stream.of("PARTNERS", "PRODUCT_APPROVALS");
                        default -> java.util.stream.Stream.of(menu);
                    })
                    .distinct()
                    .toList();
        }
        return adminRoleMenuRepository.findMenuCodesByRoleCodes(roleCodes)
                .stream()
                .distinct()
                .toList();
    }
}
