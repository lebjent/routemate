package com.trip.routemate.partner.domain;

import com.trip.routemate.user.domain.UserMstr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PARTNER_USER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PartnerUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARTNER_USER_ID")
    private Long partnerUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PARTNER_ID", nullable = false)
    private PartnerCompany partner;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private UserMstr user;

    @Column(name = "PARTNER_ROLE", nullable = false, length = 20)
    private String partnerRole;

    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default private String useYn = "Y";

    @CreationTimestamp @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;
    @UpdateTimestamp @Column(name = "MDFY_DT", nullable = false)
    private LocalDateTime mdfyDt;
}
