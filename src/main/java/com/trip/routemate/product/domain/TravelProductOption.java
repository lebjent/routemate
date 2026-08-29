package com.trip.routemate.product.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TB_TRAVEL_PRODUCT_OPTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
/** 하나의 여행 옵션 상품에서 실제로 판매되는 가격·이용 조건 단위다. */
public class TravelProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTION_ID")
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private TravelProduct product;

    @Column(name = "OPTION_NAME", nullable = false, length = 150)
    private String optionName;

    @Column(name = "OPTION_DESC", length = 500)
    private String optionDesc;

    @Column(name = "PRICE", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency;

    @Column(name = "CANCELLATION_POLICY", length = 500)
    private String cancellationPolicy;

    @Column(name = "VALIDITY_TEXT", length = 200)
    private String validityText;

    @Column(name = "CONFIRMATION_TYPE", nullable = false, length = 20)
    private String confirmationType;

    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;
}
