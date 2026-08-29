package com.trip.routemate.partner.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PARTNER_COMPANY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/** 옵션 상품을 공급하는 파트너사의 사업자 정보를 보관한다. */
public class PartnerCompany {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PARTNER_ID")
    private Long partnerId;

    @Column(name = "PARTNER_CODE", nullable = false, unique = true, length = 30)
    private String partnerCode;
    @Column(name = "PARTNER_NAME", nullable = false, length = 120)
    private String partnerName;
    @Column(name = "BUSINESS_NUMBER", unique = true, length = 30)
    private String businessNumber;
    @Column(name = "REPRESENTATIVE_NAME", length = 50)
    private String representativeName;
    @Column(name = "MANAGER_NAME", length = 50)
    private String managerName;
    @Column(name = "MANAGER_EMAIL", length = 100)
    private String managerEmail;
    @Column(name = "MANAGER_PHONE", length = 30)
    private String managerPhone;
    @Column(name = "WEBSITE_URL", length = 500)
    private String websiteUrl;
    @Column(name = "COMMISSION_RATE", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;
    @Column(name = "CONTRACT_START_DATE")
    private LocalDate contractStartDate;
    @Column(name = "CONTRACT_END_DATE")
    private LocalDate contractEndDate;
    @Column(name = "PARTNER_STATUS", nullable = false, length = 20)
    private String partnerStatus;
    @Column(name = "MEMO", length = 1000)
    private String memo;
    @CreationTimestamp @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;
    @UpdateTimestamp @Column(name = "MDFY_DT", nullable = false)
    private LocalDateTime mdfyDt;

    public void update(String partnerCode, String partnerName, String businessNumber, String representativeName,
                       String managerName, String managerEmail, String managerPhone, String websiteUrl,
                       BigDecimal commissionRate, LocalDate contractStartDate, LocalDate contractEndDate,
                       String partnerStatus, String memo) {
        this.partnerCode = partnerCode;
        this.partnerName = partnerName;
        this.businessNumber = businessNumber;
        this.representativeName = representativeName;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.managerPhone = managerPhone;
        this.websiteUrl = websiteUrl;
        this.commissionRate = commissionRate;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.partnerStatus = partnerStatus;
        this.memo = memo;
    }
}
