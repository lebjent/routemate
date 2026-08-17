package com.trip.routemate.admin.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AdminRolePolicy {

    public static final String ADMIN = "ADMIN";
    public static final String MASTER = "MASTER";
    public static final String SENIOR = "SENIOR";
    public static final String JUNIOR = "JUNIOR";

    private static final Set<String> STAFF_ROLES = Set.of(ADMIN, MASTER, SENIOR, JUNIOR);
    private static final Map<String, Set<AdminPermission>> PERMISSIONS = Map.of(
            ADMIN, EnumSet.allOf(AdminPermission.class),
            MASTER, EnumSet.of(
                    AdminPermission.DASHBOARD_VIEW,
                    AdminPermission.MEMBER_VIEW,
                    AdminPermission.MEMBER_STATUS_UPDATE,
                    AdminPermission.STAFF_VIEW,
                    AdminPermission.PLAN_MANAGE,
                    AdminPermission.DESTINATION_MANAGE
            ),
            SENIOR, EnumSet.of(
                    AdminPermission.DASHBOARD_VIEW,
                    AdminPermission.MEMBER_VIEW,
                    AdminPermission.PLAN_MANAGE
            ),
            JUNIOR, EnumSet.of(AdminPermission.DASHBOARD_VIEW)
    );

    private AdminRolePolicy() {
    }

    public static boolean isStaffRole(String role) {
        return role != null && STAFF_ROLES.contains(role);
    }

    public static Set<String> staffRoles() {
        return STAFF_ROLES;
    }

    public static List<String> permissionNamesFor(String role) {
        return permissionsFor(role).stream().map(Enum::name).sorted().toList();
    }

    public static List<GrantedAuthority> authoritiesFor(String role) {
        var authorities = permissionsFor(role).stream()
                .map(permission -> (GrantedAuthority) new SimpleGrantedAuthority(permission.name()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        return List.copyOf(authorities);
    }

    private static Set<AdminPermission> permissionsFor(String role) {
        return PERMISSIONS.getOrDefault(role, Set.of());
    }
}
