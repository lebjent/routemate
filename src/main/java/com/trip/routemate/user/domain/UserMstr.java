package com.trip.routemate.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_USER_MSTR")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserMstr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USER_EMAIL", nullable = false, unique = true, length = 100)
    private String userEmail;

    @Column(name = "USER_PWD", length = 255)
    private String userPwd;

    @Column(name = "USER_NICKNM", nullable = false, length = 50)
    private String userNicknm;

    @Column(name = "SNS_PROVIDER", nullable = false, length = 20)
    @Builder.Default
    private String snsProvider = "LOCAL";

    @Column(name = "SNS_PROVIDER_ID", length = 100)
    private String snsProviderId;

    @Column(name = "USER_ROLE", nullable = false, length = 20)
    @Builder.Default
    private String userRole = "USER";

    @Column(name = "USER_STAT_CD", nullable = false, length = 20)
    @Builder.Default
    private String userStatCd = "ACTIVE";

    @Column(name = "DEL_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String delYn = "N";

    @Column(name = "USER_PHONE", length = 20)
    private String userPhone;

    @Column(name = "USER_ZIPCODE", length = 10)
    private String userZipcode;

    @Column(name = "USER_ADDR", length = 200)
    private String userAddr;

    @Column(name = "USER_ADDR_DETAIL", length = 200)
    private String userAddrDetail;

    @Column(name = "USER_BIRTH", length = 10)
    private String userBirth;

    @CreationTimestamp // 데이터가 인서트될 때 오라클 SYSDATE를 자동으로 박아줍니다.
    @Column(name = "JOIN_DT", nullable = false, updatable = false)
    private LocalDateTime joinDt;

    @UpdateTimestamp // 데이터가 수정될 때 자동으로 현재 시간을 갱신합니다.
    @Column(name = "MDFY_DT", nullable = false)
    private LocalDateTime mdfyDt;

    public void updateStatus(String userStatCd) {
        this.userStatCd = userStatCd;
    }

    public void updateRole(String userRole) {
        this.userRole = userRole;
    }

    public void updatePassword(String encodedPassword) {
        this.userPwd = encodedPassword;
    }
}
