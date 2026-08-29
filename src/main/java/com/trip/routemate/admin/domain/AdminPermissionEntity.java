package com.trip.routemate.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_ADMIN_PERMISSION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
/** 관리자 역할에 부여할 세부 기능 권한의 마스터 엔티티다. */
public class AdminPermissionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "PERMISSION_ID")
    private Long permissionId;
    @Column(name = "PERMISSION_CODE", nullable = false, unique = true, length = 100)
    private String permissionCode;
    @Column(name = "PERMISSION_NAME", nullable = false, length = 100)
    private String permissionName;
    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;
}
