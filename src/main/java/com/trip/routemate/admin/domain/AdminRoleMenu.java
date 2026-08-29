package com.trip.routemate.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_ADMIN_ROLE_MENU")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
/** 관리자 역할과 접근 가능한 메뉴를 연결하는 매핑 엔티티다. */
public class AdminRoleMenu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "ROLE_MENU_ID")
    private Long roleMenuId;
    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;
    @Column(name = "MENU_ID", nullable = false)
    private Long menuId;
    @Column(name = "PERMISSION_ID")
    private Long permissionId;
    @Column(name = "ALLOW_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String allowYn;
}
