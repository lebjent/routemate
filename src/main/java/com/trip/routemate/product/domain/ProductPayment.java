package com.trip.routemate.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 주문에 대한 결제 시도와 금액을 보관한다. 실제 PG 연동 전에는 MOCK 결제 키를 사용한다. */
@Entity
@Table(name = "TB_PRODUCT_PAYMENT")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @Builder @AllArgsConstructor
public class ProductPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long paymentId;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ORDER_ID", nullable = false, unique = true) private ProductOrder order;
    @Column(name = "PAYMENT_KEY", nullable = false, unique = true, length = 100) private String paymentKey;
    @Column(name = "AMOUNT", nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(name = "CURRENCY", nullable = false, length = 3) private String currency;
    @Column(name = "STATUS", nullable = false, length = 20) @Builder.Default private String status = "READY";
    @Column(name = "METHOD", nullable = false, length = 20) @Builder.Default private String method = "MOCK";
    @CreationTimestamp @Column(name = "CREATE_DT", nullable = false, updatable = false) private LocalDateTime createDt;
    public void complete() { status = "PAID"; }
    public void fail() { status = "FAILED"; }
}
