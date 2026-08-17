package com.trip.routemate.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_ADMIN_USER_ROLE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class AdminUserRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "USER_ROLE_ID")
    private Long userRoleId;
    @Column(name = "USER_ID", nullable = false)
    private Long userId;
    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;
    @Column(name = "PRIMARY_YN", nullable = false, length = 1)
    private String primaryYn;
}
