package com.trip.routemate.product.domain;

import com.trip.routemate.user.domain.UserMstr;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TB_PRODUCT_ORDER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/** 사용자가 구매한 옵션 상품의 예약 정보와 이용 상태를 보관하는 엔티티다. */
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID")
    private Long orderId;

    @Column(name = "ORDER_NO", nullable = false, unique = true, length = 40)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserMstr user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private TravelProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_ID")
    private TravelProductOption option;

    @Column(name = "PRODUCT_NAME", nullable = false, length = 150)
    private String productName;

    @Column(name = "OPTION_NAME", nullable = false, length = 150)
    private String optionName;

    @Column(name = "PRODUCT_IMAGE_URL", length = 500)
    private String productImageUrl;

    @Column(name = "DESTINATION_NAME", nullable = false, length = 150)
    private String destinationName;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @Column(name = "UNIT_PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "TOTAL_PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;

    @Column(name = "USE_DATE", nullable = false)
    private LocalDate useDate;

    @Column(name = "BUYER_NAME", nullable = false, length = 50)
    private String buyerName;

    @Column(name = "BUYER_EMAIL", nullable = false, length = 100)
    private String buyerEmail;

    @Column(name = "BUYER_PHONE", length = 20)
    private String buyerPhone;

    @Column(name = "ORDER_STATUS", nullable = false, length = 20)
    @Builder.Default
    private String orderStatus = "ORDERED";

    @Column(name = "PAYMENT_STATUS", nullable = false, length = 20)
    @Builder.Default
    private String paymentStatus = "PENDING";

    @CreationTimestamp
    @Column(name = "CREATE_DT", nullable = false, updatable = false)
    private LocalDateTime createDt;

    /** 한 주문에 포함된 옵션별 수량·단가 스냅샷이다. 기존 단일 주문은 비어 있을 수 있다. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductOrderItem> items = new ArrayList<>();

    /** 주문 항목을 추가하고 주문 헤더와의 연관관계를 유지한다. */
    public void addItem(ProductOrderItem item) {
        items.add(item);
    }

    public void changeStatus(String orderStatus, String paymentStatus) {
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
    }
}
