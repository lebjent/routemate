package com.trip.routemate.plan.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TB_TRAVEL_PACKING_ITEM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelPackingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PACKING_ITEM_ID")
    private Long packingItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PLAN_ID", nullable = false)
    private TravelPlan travelPlan;

    @Column(name = "ITEM_NAME", nullable = false, length = 100)
    private String itemName;

    @Column(name = "REQUIRED_YN", nullable = false, length = 1)
    private String requiredYn;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;
}
