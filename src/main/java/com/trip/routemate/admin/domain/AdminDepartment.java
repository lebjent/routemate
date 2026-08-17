package com.trip.routemate.admin.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_ADMIN_DEPT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class AdminDepartment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "DEPT_ID")
    private Long deptId;
    @Column(name = "DEPT_CODE", nullable = false, unique = true, length = 50)
    private String deptCode;
    @Column(name = "DEPT_NAME", nullable = false, length = 100)
    private String deptName;
    @Column(name = "USE_YN", nullable = false, length = 1)
    private String useYn;
}
