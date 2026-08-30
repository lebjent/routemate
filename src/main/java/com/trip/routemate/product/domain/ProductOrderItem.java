package com.trip.routemate.product.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** 한 상품 주문 안에 담긴 옵션별 수량·단가 스냅샷이다. */
@Entity
@Table(name = "TB_PRODUCT_ORDER_ITEM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ITEM_ID")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private ProductOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_ID")
    private TravelProductOption option;

    @Column(name = "OPTION_NAME", nullable = false, length = 150)
    private String optionName;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @Column(name = "UNIT_PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "TOTAL_PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;
}
