package com.trip.routemate.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_ADMIN_ROLE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
/** 관리자 직원에게 부여하는 역할 코드와 표시명을 보관한다. */
public class AdminRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "ROLE_ID")
    private Long roleId;
    @Column(name = "ROLE_CODE", nullable = false, unique = true, length = 50)
    private String roleCode;
    @Column(name = "ROLE_NAME", nullable = false, length = 100)
    private String roleName;
    @Column(name = "ROLE_LEVEL", nullable = false)
    private Integer roleLevel;
    @Column(name = "DEPT_ID")
    private Long deptId;
    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;
}
