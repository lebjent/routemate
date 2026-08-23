package com.trip.routemate.product.domain;

import com.trip.routemate.user.domain.UserMstr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_PRODUCT_APPROVAL_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductApprovalHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPROVAL_HISTORY_ID") private Long approvalHistoryId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRODUCT_ID", nullable = false) private TravelProduct product;
    @Column(name = "DECISION_STATUS", nullable = false, length = 20) private String decisionStatus;
    @Column(name = "DECISION_REASON", length = 500) private String decisionReason;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "APPROVER_USER_ID", nullable = false) private UserMstr approver;
    @CreationTimestamp
    @Column(name = "DECISION_DT", nullable = false, updatable = false) private LocalDateTime decisionDt;
}
