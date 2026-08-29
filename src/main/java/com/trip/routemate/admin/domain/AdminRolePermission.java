package com.trip.routemate.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_ADMIN_ROLE_PERMISSION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
/** 관리자 역할과 세부 기능 권한을 연결하는 매핑 엔티티다. */
public class AdminRolePermission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "ROLE_PERMISSION_ID")
    private Long rolePermissionId;
    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;
    @Column(name = "PERMISSION_ID", nullable = false)
    private Long permissionId;
    @Column(name = "ALLOW_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String allowYn;
}
